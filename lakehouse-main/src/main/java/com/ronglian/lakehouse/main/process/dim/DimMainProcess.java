package com.ronglian.lakehouse.main.process.dim;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ronglian.lakehouse.main.entity.TableProcess;
import com.ronglian.lakehouse.main.util.ConfigUtil;
import com.ronglian.lakehouse.main.util.DimSinkFunction;
import com.ronglian.lakehouse.main.util.KafkaUtil;
import com.ronglian.lakehouse.main.util.TableProcessFunction;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.datastream.BroadcastConnectedStream;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

public class DimMainProcess {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        System.setProperty("HADOOP_USER_NAME","hadoop");

        String baseDbTopic = "ODS_BASE_DB";
        String baseDbGroupId = "ODS_BASE_DB_GROUP";

        FlinkKafkaConsumer<String> baseDbStreams = KafkaUtil.getFlinkKafkaConsumer(baseDbTopic, baseDbGroupId);
        DataStreamSource<String> baseDbStreamSource = env.addSource(baseDbStreams);

        // 过滤掉非JSON数据&保留新增、变化以及初始化数据并将数据转换为JSON格式
        SingleOutputStreamOperator<JSONObject> streamOperator = baseDbStreamSource.flatMap((FlatMapFunction<String, JSONObject>) (value, out) -> {
            try {
                JSONObject jsonObject = JSON.parseObject(value);
                String type = jsonObject.getString("type");

                if ("insert".equals(type) || "update".equals(type) || "bootstrap-insert".equals(type)) {
                    out.collect(jsonObject);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 获取MySQL配置信息
        Properties mysqlConfigs = ConfigUtil.getConfigs("system.properties");
        // 使用FlinkCDC读取MySQL配置信息表创建配置流
        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname(mysqlConfigs.getProperty("datasource.mysql.hostname"))
                .port(3306)
                .username(mysqlConfigs.getProperty("datasource.mysql.username"))
                .password(mysqlConfigs.getProperty("datasource.mysql.password"))
                .databaseList("gmall_config")
                .tableList("gmall_config.table_process")
                .startupOptions(StartupOptions.initial())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        DataStreamSource<String> mysqlSourceDS = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "mysql");

        // 配置MySQL为广播流
        MapStateDescriptor<String, TableProcess> stateDescriptor = new MapStateDescriptor<>("map-status", String.class, TableProcess.class);
        BroadcastStream<String> mysqlBroadcastStream = mysqlSourceDS.broadcast(stateDescriptor);

        // 主流连接广播流

        BroadcastConnectedStream<JSONObject, String> connectedStream = streamOperator.connect(mysqlBroadcastStream);
        //处理连接流,根据配置信息处理主流数据
        SingleOutputStreamOperator<JSONObject> configStream = connectedStream.process(new TableProcessFunction(stateDescriptor));

        configStream.addSink(new DimSinkFunction());
        env.execute("DIM_APP");
    }



}
