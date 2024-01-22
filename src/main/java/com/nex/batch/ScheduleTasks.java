package com.nex.batch;

import com.nex.common.CommonStaticUtil;
import com.nex.common.ConfigData;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Component;

import java.time.Duration;
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
                // 스케쥴러가 시작되는 부분
                // scheduler.schedule(getRunnable(), getTrigger(batchCycleByHour));
                scheduler.schedule(getRunnable(), new CronTrigger(String.format("0 0 */%s * * *", batchCycleByHour + "")));

            }
        }
    }

    private Runnable getRunnable(){
        return () -> {
//            log.info("current time === {}", CommonStaticUtil.getCurrent().getSecond() + "");
            trackingTask();
        };
    }

//    private Trigger getTrigger(int batchCycleByHour) {
//        // 작업 주기 설정
//        log.info("Scheduled Cycle Setting by " + batchCycleByHour + " hours");
////        return new PeriodicTrigger(batchCycleByHour, TimeUnit.HOURS);
//
//
//        PeriodicTrigger trigger = new PeriodicTrigger(batchCycleByHour, TimeUnit.HOURS);
//
//        // Trigger의 시작 시간을 조절하여 00시, 06시, 12시, 18시, 24시로 설정
//        int currentHour = CommonStaticUtil.getCurrent().getHour();  // 현재 시간을 가져오는 메소드를 구현해야 합니다.
//        int nextExecutionHour = ((currentHour / batchCycleByHour) + 1) * batchCycleByHour;
//
//        trigger.setInitialDelay(nextExecutionHour - currentHour);
//
//        return trigger;
//    }

    //@Scheduled(cron = "${batch.schedule.tracking.cron}", zone = "Asia/Seoul")
    //@Scheduled(cron = "0 12 * * * *", zone = "Asia/Seoul")
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


//    @Scheduled(fixedDelay = 5000, initialDelay = 3000)
//    public void init() {
//
//    }
}