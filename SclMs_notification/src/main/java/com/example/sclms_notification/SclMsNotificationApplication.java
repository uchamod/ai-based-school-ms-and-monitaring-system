package com.example.sclms_notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SclMsNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(SclMsNotificationApplication.class, args);
    }

}
