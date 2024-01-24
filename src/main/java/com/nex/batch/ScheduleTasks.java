package com.nex.batch;

import com.nex.common.CommonStaticUtil;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
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
    private ThreadPoolTaskScheduler scheduler;

    public void reStartScheduler() {
        if(scheduler != null)
            scheduler.shutdown();

        Integer batchCycleByHour = ConfigDataManager.getInstance().getDefaultConfig().getBatchCycleByHour();
        Boolean isBatchFlag = ConfigDataManager.getInstance().getDefaultConfig().getIsBatchFlag();
        if(isBatchFlag != null && isBatchFlag == true) {
            log.info("batch -- start");
            if (batchCycleByHour != null && batchCycleByHour > 0) {
                scheduler = new ThreadPoolTaskScheduler();
                scheduler.initialize();
                scheduler.schedule(getRunnable(), new CronTrigger(String.format("0 0 */%s * * *", batchCycleByHour + "")));

            }
        }
    }

    private Runnable getRunnable(){
        return () -> {
            log.info("current time === {}", CommonStaticUtil.getCurrent().getSecond() + "");
            trackingTask();
        };
    }
    public void trackingTask() {
        log.info("trackingTask 진입");
        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }
}