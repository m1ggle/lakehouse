package com.ronglian.lakehouse.main.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Properties;

@DisplayName("配置文件获取工具类测试")
class ConfigUtilTest {

    @DisplayName("获取配置文件")
    @Test
    public void getConfigsTest(){
        Properties configs = ConfigUtil.getConfigs("kafka.properties");
        System.out.println(configs.getProperty("kafka.bootstrap.server"));
    }

}