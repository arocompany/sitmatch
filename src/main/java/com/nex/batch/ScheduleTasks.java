package com.nex.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
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

    private final Job job;

    private final JobLauncher jobLauncher;


    /**
     * 추적 이력
     */
    //@Scheduled(cron = "${batch.schedule.tracking.cron}", zone = "Asia/Seoul")
    //@Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 3 * * * *", zone = "Asia/Seoul")
    public void trackingTask() {
        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
        //TODO : 임시
        try {
            jobLauncher.run(job, jobParameters);
            //trackingBatchConfiguration.jobLauncher().run(trackingBatchConfiguration.run(), new JobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }

}