package com.nex.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 스케쥴
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleTasks {

    private final TrackingBatchConfiguration trackingBatchConfiguration;


    /**
     * 추적 이력
     */
    @Scheduled(cron = "${batch.schedule.tracking.cron}", zone = "Asia/Seoul")
    //@Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    //@Scheduled(cron = "0 21 * * * *", zone = "Asia/Seoul")
    public void trackingTask() {
        //TODO : 임시
        try {
            trackingBatchConfiguration.jobLauncher().run(trackingBatchConfiguration.run(), new JobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }

}