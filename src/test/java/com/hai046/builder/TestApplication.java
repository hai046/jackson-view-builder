package com.hai046.builder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.hai046"})
//@MapperScan(basePackages = "com.hai046")
@EnableBuilder(basePackages = "com.hai046")
@EnableAsync
public class TestApplication {

    public static void main(String[] args) {

        SpringApplication.run(new Class[]{TestApplication.class}, args);
    }
}
