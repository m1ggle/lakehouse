package com.ronglian.lakehouse.main.process.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.ronglian.lakehouse.main.util.DateFormatUtil;
import com.ronglian.lakehouse.main.util.KafkaUtil;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.RichFilterFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * &#064;description  流量域独立访客事务事实表。过滤页面数据中的独立访客访问记录，即将用户首日访问的记录输出到特定topic的kafka流中，当日其他的访问记录丢弃掉
 * 数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
 * 程  序：Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUniqueVisitorDetail -> Kafka(ZK)
 */
public class DwdTrafficUniqueVisitorDetail {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 通用配置
        env.setParallelism(1);
//       //开启checkpoint
//        env.enableCheckpointing(5*60000L, CheckpointingMode.EXACTLY_ONCE);
//        env.getCheckpointConfig().setCheckpointTimeout(10*60000L);
//        env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
//        //重启策略
//        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,5000L));
//        //设置后端状态
//        env.setStateBackend(new HashMapStateBackend());
//        env.getCheckpointConfig().setCheckpointStorage("hdfs://kubemaster:8020/flink/check_point");
        //设置本地运行用户
        System.setProperty("HADOOP_USER_NAME","hadoop");

        String topic = "TOPIC_DWD_LOG_PAGE";
        String groupId = "DWD_TRAFFIC_PAGE_LOG_GROUP";


        DataStreamSource<String> kafkaDataSource = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo: 1 过滤掉上一跳页面不为null的数据并将每行数据转换为JSON对象
        /*
         * 状态记录mid末次登录日期，如果末次登录日期为null或者不是今日，则本次访问时改mid当日首次访问，保留数据，将末次登录日期更新为当日
         * 否则不是当日首次访问，丢弃数据
         */
        SingleOutputStreamOperator<JSONObject> jsonObjectSingleOutputStreamOperator = kafkaDataSource.flatMap((FlatMapFunction<String, JSONObject>) (value, out) -> {
            JSONObject jsonObject = JSON.parseObject(value);
            String lastPageId = jsonObject.getJSONObject("page").getString("last_page_id");
            if (lastPageId == null) {
                out.collect(jsonObject);
            }
        });

        //todo: 2 按照Mid分组
        KeyedStream<JSONObject, String> keyedStream = jsonObjectSingleOutputStreamOperator.keyBy(json -> json.getJSONObject("common").getString("mid"));

        //todo:3 使用状态编程实现按照Mid的去重

        SingleOutputStreamOperator<JSONObject> uvDS = keyedStream.filter(new RichFilterFunction<JSONObject>() {
            private ValueState<String> lastVisitSate;

            @Override
            public void open(Configuration parameters) {

                ValueStateDescriptor<String> stateDescriptor = new ValueStateDescriptor<>("last-visit", String.class);
                // 设置状态的TTL
                StateTtlConfig stateTtlConfig = new StateTtlConfig
                        .Builder(Time.days(1))
                        .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                        .build();
                stateDescriptor.enableTimeToLive(stateTtlConfig);

                lastVisitSate = getRuntimeContext().getState(stateDescriptor);
            }

            @Override
            public boolean filter(JSONObject value) throws Exception {

                String lastDate = lastVisitSate.value();
                Long ts = value.getLong("ts");
                String curDate = DateFormatUtil.toDate(ts);

                if (lastDate == null || !lastDate.equals(curDate)) {
                    lastVisitSate.update(curDate);
                    return true;
                } else {
                    return false;
                }
            }
        });
        // todo: 将数据写到kafka
        String targetTopic = "dwd_traffic_unique_visitor_detail";

        uvDS.map(JSONAware::toJSONString).addSink(KafkaUtil.getFlinkKafkaProducer(targetTopic));

        env.execute("DwdTrafficUniqueVisitorDetail");




    }
}
