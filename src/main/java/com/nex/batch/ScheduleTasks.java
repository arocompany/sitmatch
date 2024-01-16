package com.nex.batch;

import com.nex.common.ConfigDataManager;
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
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 스케쥴
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleTasks {

    private final Job job;
    private final JobLauncher jobLauncher;

    private ThreadPoolTaskScheduler scheduler;

    public void stopScheduler(){
        if(scheduler != null)
        scheduler.shutdown();
    }

    public void startScheduler() {
        Integer batchCycleByHour = ConfigDataManager.getInstance().getDefaultConfig().getBatchCycleByHour();
        if(batchCycleByHour != null && batchCycleByHour > 0) {
            scheduler = new ThreadPoolTaskScheduler();
            scheduler.initialize();
            // 스케쥴러가 시작되는 부분
            scheduler.schedule(getRunnable(), getTrigger(batchCycleByHour));
        }
    }

    private Runnable getRunnable(){
        return () -> {
            trackingTask();
        };
    }

    private Trigger getTrigger(int batchCycleByHour) {
        // 작업 주기 설정
        log.info("Scheduled Cycle Setting by " + batchCycleByHour + " hours");
        return new PeriodicTrigger(batchCycleByHour, TimeUnit.HOURS);
    }

    //@Scheduled(cron = "${batch.schedule.tracking.cron}", zone = "Asia/Seoul")
    //@Scheduled(cron = "0 12 * * * *", zone = "Asia/Seoul")
    public void trackingTask() {
        Boolean isBatchFlag = ConfigDataManager.getInstance().getDefaultConfig().getIsBatchFlag();
        log.info("trackingTask 진입, isBatchFlag === " + isBatchFlag);

        if(isBatchFlag != null && isBatchFlag == true) {
            JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
            //TODO : 임시
            try {
                jobLauncher.run(job, jobParameters);
            } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                     JobParametersInvalidException e) {
                throw new RuntimeException(e);
            }
        }
    }
}