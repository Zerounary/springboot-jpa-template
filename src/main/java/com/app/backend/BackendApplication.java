package com.app.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
@MapperScan("com.app.backend.repository")
public class BackendApplication {
    public static void main(String[] args) {
        // Set default timezone to Beijing time (Asia/Shanghai)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(BackendApplication.class, args);
    }
}
