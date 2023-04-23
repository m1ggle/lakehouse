package com.ronglian.lakehouse.main.util;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Properties;

public class KafkaUtil {

    private static Properties properties = ConfigUtil.getConfigs("kafka.properties");

    /**
     * flink kafka source function
     * @param topic
     * @param groupId
     * @return
     */
    public static FlinkKafkaConsumer<String> getFlinkKafkaConsumer(String topic, String groupId){
        Properties prop = new Properties();
        prop.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getProperty("kafka.bootstrap.server"));
        prop.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");
        return new FlinkKafkaConsumer<String>(topic, new KafkaDeserializationSchema<String>() {
            @Override
            public boolean isEndOfStream(String s) {
                return false;
            }

            @Override
            public String deserialize(ConsumerRecord<byte[], byte[]> consumerRecord) throws Exception {
                if (consumerRecord == null || consumerRecord.value() == null){
                    return "";
                }else {
                    return new String(consumerRecord.value(), "UTF-8");
                }
            }

            @Override
            public TypeInformation<String> getProducedType() {
                return BasicTypeInfo.STRING_TYPE_INFO;
            }
        }, prop);
    }

    /**
     * fink kafka producer function
     * @param topic
     * @return
     */
    public static FlinkKafkaProducer<String> getFlinkKafkaProducer( String topic){
        return new FlinkKafkaProducer<>(properties.getProperty("kafka.bootstrap.server"), topic, new SimpleStringSchema());

    }



}
