package com.nex;

import com.nex.batch.ScheduleTasks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
@RequiredArgsConstructor
public class SitMatchApplication implements ApplicationListener<ContextRefreshedEvent> {
    private final ScheduleTasks scheduleTasks;
    public static void main(String[] args) {
        SpringApplication.run(SitMatchApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("application is boot up!");
        scheduleTasks.stopScheduler();
        scheduleTasks.startScheduler();
    }
}