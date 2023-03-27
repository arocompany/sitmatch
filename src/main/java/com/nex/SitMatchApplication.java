package com.nex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//2023-03-24 추적 이력으로 인해 추가
@EnableScheduling
public class SitMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SitMatchApplication.class, args);
    }

}