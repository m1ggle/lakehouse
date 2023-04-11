package org.yahveo.app.dim;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.yahveo.util.KafkaUtil;

/**
 * @author ywren
 */
public class DimApp {

    public static void main(String[] args) {
        //todo: 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 成产环境设置为kafka主题的分区数
        env.setParallelism(1);
        //开启checkpoint
        env.enableCheckpointing(5*60000L, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointTimeout(10*60000L);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        //重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,5000L));

        //设置后端状态
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://bighdp_mian:8020/flink/check_point");
        //设置本地运行用户
        System.setProperty("HADOOP_USER_NAME","yahve");

        String topic = "";
        String groupId = "";

        DataStreamSource<String> kafkaStreamSource = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));


    }

}
