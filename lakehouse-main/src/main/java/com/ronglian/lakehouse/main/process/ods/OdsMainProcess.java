package com.ronglian.lakehouse.main.process.ods;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.ronglian.lakehouse.main.common.Constant;
import com.ronglian.lakehouse.main.util.DateFormatUtil;
import com.ronglian.lakehouse.main.util.KafkaUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yawei
 */
public class OdsMainProcess {

    private static final String START = "start";
    private static final String ACTION = "actions";
    private static final String DISPLAY = "displays";
    private static final String ERR = "err";
    private static final String PAGE = "page";

    public static void main(String[] args) throws Exception {
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

        String topic = "ODS_BASE_LOG";
        String groupId = "ODS_BASE_LOG_GROUP";

        SingleOutputStreamOperator<String> kafkaStreamSource = env.addSource(KafkaUtil.getFlinkKafkaConsumer(topic, groupId));
        // 数据结构转换为JSON
        SingleOutputStreamOperator<JSONObject> outputStreamOperator = null;

        try {
            outputStreamOperator = kafkaStreamSource.filter((FilterFunction<String>) value -> {
                try {
                    JSON.parseObject(value);
                    return true;
                }catch (Exception e){
                    return false;
                }
            }).map(JSON::parseObject);
        }catch (Exception e){
            e.printStackTrace();
        }

        // 按mid进行分组
        KeyedStream<JSONObject, String> keyedStream = outputStreamOperator.keyBy(json -> json.getJSONObject("common").getString("mid"));
        // 新增新老用户判断
        SingleOutputStreamOperator<JSONObject> mapStream = keyedStream.map(new RichMapFunction<JSONObject, JSONObject>() {
            private ValueState<String> lastVisitState;

            @Override
            public void open(Configuration parameters) throws Exception {
                lastVisitState = getRuntimeContext().getState(new ValueStateDescriptor<String>("last-visit", String.class));
            }

            @Override
            public JSONObject map(JSONObject value) throws Exception {
                String isNew = value.getJSONObject("common").getString("is_new");
                Long ts = value.getLong("ts");
                String localDate = DateFormatUtil.toDate(ts);

                String lastVisitDate = lastVisitState.value();

                if ("1".equals(isNew)) {
                    if (lastVisitDate == null) {
                        lastVisitState.update(localDate);
                    } else if (!lastVisitDate.equals(localDate)) {
                        value.getJSONObject("common").put("is_new", "0");
                    }
                } else if (lastVisitDate == null) {
                    lastVisitState.update(DateFormatUtil.toDate(ts - 24 * 60 * 60 * 1000L));

                }
                return value;
            }
        });

        Map<String, DataStream<JSONObject>> splitStream = splitStream(mapStream);

        // 写出数据
        splitStream.get(PAGE)
                .map((MapFunction<JSONObject, String>) JSONAware::toJSONString)
                .addSink(KafkaUtil.getFlinkKafkaProducer(Constant.PAGE_TOPIC));
        splitStream.get(ACTION)
                .map((MapFunction<JSONObject, String>) JSONAware::toJSONString)
                .addSink(KafkaUtil.getFlinkKafkaProducer(Constant.ACTION_TOPIC));
        splitStream.get(START)
                .map((MapFunction<JSONObject, String>) JSONAware::toJSONString)
                .addSink(KafkaUtil.getFlinkKafkaProducer(Constant.START_TOPIC));
        splitStream.get(DISPLAY)
                .map((MapFunction<JSONObject, String>) JSONAware::toJSONString)
                .addSink(KafkaUtil.getFlinkKafkaProducer(Constant.DISPLAY_TOPIC));
        splitStream.get(ERR)
                .map((MapFunction<JSONObject, String>) JSONAware::toJSONString)
                .addSink(KafkaUtil.getFlinkKafkaProducer(Constant.ERROR_TOPIC));

        env.execute();

	}

    /**
     * 分流函数
     * @param stream
     * @return
     */
    public static Map<String, DataStream<JSONObject>> splitStream(SingleOutputStreamOperator<JSONObject> stream) {
        /**
         * 分流输出：一共五个流分别存储5中日志
         *  start action display page err
         *  start -> 主流
         *  其他 -> 侧流输出
         *
         */
        OutputTag<JSONObject> displayTag = new OutputTag<JSONObject>("display") {};
        OutputTag<JSONObject> actionTag = new OutputTag<JSONObject>("action") {};
        OutputTag<JSONObject> errTag = new OutputTag<JSONObject>("err") {};
        OutputTag<JSONObject> pageTag = new OutputTag<JSONObject>("page") {};
        OutputTag<JSONObject> startTag = new OutputTag<JSONObject>("start") {};


        SingleOutputStreamOperator<JSONObject> startSideOutput = stream.process(new ProcessFunction<JSONObject, JSONObject>() {

            @Override
            public void processElement(JSONObject value, ProcessFunction<JSONObject, JSONObject>.Context ctx, Collector<JSONObject> out) throws Exception {
                //所有数据都需要包含的公共信息
                JSONObject common = value.getJSONObject("common");
                // 启动页面
                JSONObject startJsonObj = value.getJSONObject(START);
                if (startJsonObj != null) {
                    out.collect(startJsonObj);
                } else {
                    // display
                    JSONArray displayJsonArray = value.getJSONArray(DISPLAY);
                    if (displayJsonArray != null) {
                        for (int i = 0; i < displayJsonArray.size(); i++) {
                            JSONObject jsonObject = displayJsonArray.getJSONObject(i);
                            jsonObject.putAll(common);
                            jsonObject.put("ts", value.getLong("ts"));
                            ctx.output(displayTag, jsonObject);
                        }
                        value.remove(DISPLAY);
                    }
                    // actions
                    JSONArray actionJsonArray = value.getJSONArray(ACTION);
                    if (actionJsonArray != null) {
                        for (int i = 0; i < actionJsonArray.size(); i++) {
                            JSONObject jsonObject = actionJsonArray.getJSONObject(i);
                            jsonObject.putAll(common);
                            ctx.output(actionTag, jsonObject);
                        }
                        value.remove(ACTION);
                    }

                    // err
                    JSONObject errorJsonArray = value.getJSONObject(ERR);
                    if (errorJsonArray != null) {
                        ctx.output(errTag, errorJsonArray);
                        value.remove(ERR);
                    }
                    // page
                    JSONObject pageJsonArray = value.getJSONObject(PAGE);
                    if (pageJsonArray != null) {
                        ctx.output(pageTag, pageJsonArray);
                    }
                }
            }
        });
        DataStream<JSONObject> displaySideOutput = startSideOutput.getSideOutput(displayTag);
        DataStream<JSONObject> actionSideOutput = startSideOutput.getSideOutput(actionTag);
        DataStream<JSONObject> pageSideOutput = startSideOutput.getSideOutput(pageTag);
        DataStream<JSONObject> errSideOutput = startSideOutput.getSideOutput(errTag);


        Map<String, DataStream<JSONObject>> streamMap = new HashMap<>();
        streamMap.put(START,startSideOutput);
        streamMap.put(DISPLAY,displaySideOutput);
        streamMap.put(ACTION,actionSideOutput);
        streamMap.put(PAGE,pageSideOutput);
        streamMap.put(ERR,errSideOutput);

        return streamMap;
    }

}
