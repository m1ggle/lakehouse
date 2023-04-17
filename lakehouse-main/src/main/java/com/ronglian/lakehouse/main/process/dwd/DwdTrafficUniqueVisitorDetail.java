package com.ronglian.lakehouse.main.process.dwd;

import com.ronglian.lakehouse.main.util.KafkaUtil;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * &#064;description  流量域独立访客事务事实表。过滤页面数据中的独立访客访问记录，即将用户首日访问的记录输出到特定topic的kafka流中，当日其他的访问记录丢弃掉
 * 数据流：web/app -> Nginx -> 日志服务器(.log) -> Flume -> Kafka(ODS) -> FlinkApp -> Kafka(DWD) -> FlinkApp -> Kafka(DWD)
 * 程  序：Mock(lg.sh) -> Flume(f1) -> Kafka(ZK) -> BaseLogApp -> Kafka(ZK) -> DwdTrafficUniqueVisitorDetail -> Kafka(ZK)
 */
public class DwdTrafficUniqueVisitorDetail {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 通用配置
        env.setParallelism(1);
        //开启checkpoint
        env.enableCheckpointing(5*60000L, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointTimeout(10*60000L);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(2);
        //重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,5000L));
        //设置后端状态
        env.setStateBackend(new HashMapStateBackend());
        env.getCheckpointConfig().setCheckpointStorage("hdfs://kubemaster:8020/flink/check_point");
        //设置本地运行用户
        System.setProperty("HADOOP_USER_NAME","hadoop");

        String topic = "DWD_TRAFFIC_PAGE_LOG";
        String groupId = "DWD_TRAFFIC_PAGE_LOG_GROUP";


        DataStreamSource<String> kafkaDataSource = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));

        //todo: 1 过滤掉上一跳页面不为null的数据并将每行数据转换为JSON对象

        //todo: 2 按照Mid分组

        //todo:3 使用状态编程实现按照Mid的去重


    }
}
