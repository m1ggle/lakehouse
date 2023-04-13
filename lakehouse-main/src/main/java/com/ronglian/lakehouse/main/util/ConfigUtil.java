package com.ronglian.lakehouse.main.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author yawen
 * 获取配置文件信息
 */
public class ConfigUtil {

    public static Properties getConfigs(String configName) {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(configName);
        Properties prop = new Properties();
        try {
            prop.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
