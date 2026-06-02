package com.example.sclms_school_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SclMsSchoolServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SclMsSchoolServiceApplication.class, args);
    }

}
