package com.ronglian.lakehouse.main.process.dwd;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.ronglian.lakehouse.main.util.KafkaUtil;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.PatternTimeoutFunction;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

public class DwdTrafficUserJumpDetail {
    public static void main(String[] args) throws Exception {
        // todo 1.获取执行环境

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
        System.setProperty("HADOOP_USER_NAME", "hadoop");

        // todo 2.读取kafka，页面日志主题数据创建流
        String topic = "TOPIC_DWD_LOG_PAGE";
        String groupId = "DWD_TRAFFIC_USER_JUMP_GROUP";
        DataStreamSource<String> kafkaDataSource = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        // todo 3 将每行数据转为json对象,并提取时间戳生成Watermark
        SingleOutputStreamOperator<JSONObject> jsonObjDS = kafkaDataSource.map(JSON::parseObject)
                .assignTimestampsAndWatermarks(WatermarkStrategy.<JSONObject>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                        .withTimestampAssigner((SerializableTimestampAssigner<JSONObject>) (element, recordTimestamp) -> element.getLong("ts")));

        // todo 4 按照mid分组
        // 优化到todo 6
        //KeyedStream<JSONObject, String> keyedStream = jsonObjDS.keyBy(json -> json.getJSONObject("common").getString("mid"));

        // todo 5 定义cep的模式序列
        Pattern<JSONObject, JSONObject> pattern = Pattern
                .<JSONObject>begin("start").where(new SimpleCondition<JSONObject>() {
                    @Override
                    public boolean filter(JSONObject value) throws Exception {
                        String lastPageId = value.getJSONObject("page").getString("last_page_id");
                        return lastPageId == null || lastPageId.length() <= 0;
                    }
                })
                .times(2)
                //严格近邻
                .consecutive()
                .within(Time.seconds(10));

        // todo 6 将模式序列作用到流
        PatternStream<JSONObject> patternStream = CEP.pattern(jsonObjDS.keyBy(jsonObj -> jsonObj.getJSONObject("common").getString("mid")), pattern);

        // todo 7 提取事件，匹配上的时间和超时时间，匹配上的时间为主流，超时为测输出流
        OutputTag<JSONObject> timeoutTag = new OutputTag<JSONObject>("timeout") {
        };

        SingleOutputStreamOperator<JSONObject> selectedDS = patternStream.select(timeoutTag,
                (PatternTimeoutFunction<JSONObject, JSONObject>) (map, l) -> map.get("start").get(0),
                (PatternSelectFunction<JSONObject, JSONObject>) map -> map.get("start").get(0));

        DataStream<JSONObject> sideOutput = selectedDS.getSideOutput(timeoutTag);
        // todo 8 合并两种事件

        DataStream<JSONObject> unionDS = selectedDS.union(selectedDS);

        // todo 9 将数据写出到kafka
        String sinkTopic = "DWM_USER_JUMP_DETAIL";
        unionDS.map(JSONAware::toJSONString).addSink(KafkaUtil.getFlinkKafkaProducer(sinkTopic));

        // todo 10 启动作业
        env.execute(DwdTrafficUserJumpDetail.class.getName());
    }
}