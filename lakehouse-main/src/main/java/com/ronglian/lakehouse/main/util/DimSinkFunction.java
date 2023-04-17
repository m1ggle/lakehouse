package com.ronglian.lakehouse.main.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

public class DimSinkFunction extends RichSinkFunction<JSONObject> {

    DruidDataSource dataSource = null;

    @Override
    public void open(Configuration parameters) throws Exception {
        dataSource = DruidDSUtil.createDataSource();
    }

    @Override
    public void invoke(JSONObject value, Context context) throws Exception {
        DruidPooledConnection connection = dataSource.getConnection();
        String sinkTable = value.getString("sinkTable");
        JSONObject data = value.getJSONObject("data");

        PhoenixUtil.upsertValues(connection,sinkTable,data);

        connection.close();

    }
}
