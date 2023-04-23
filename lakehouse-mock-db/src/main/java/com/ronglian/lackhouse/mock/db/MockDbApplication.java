package com.ronglian.lackhouse.mock.db;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@MapperScan("com.ronglian.lackhouse.mock.db.mapper")
public class MockDbApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MockDbApplication.class, args);

        MockTask mockTask = context.getBean(MockTask.class);

        mockTask.mainTask();

    }
}
