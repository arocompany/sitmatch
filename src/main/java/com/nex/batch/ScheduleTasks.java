package com.nex.batch;

import com.nex.batch.tracking.TrackingBatchConfiguration;
import com.nex.common.CommonStaticUtil;
import com.nex.common.ConfigDataManager;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 스케쥴
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleTasks {
    private final JobLauncher jobLauncher;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private ThreadPoolTaskScheduler scheduler;
    private final SearchResultRepository searchResultRepository;

    private final TrackingBatchConfiguration trackingBatchConfiguration;

    public void reStartScheduler() {
        if(scheduler != null)
            scheduler.shutdown();

        Integer batchCycleByHour = ConfigDataManager.getInstance().getDefaultConfig().getBatchCycleByHour();
        Boolean isBatchFlag = ConfigDataManager.getInstance().getDefaultConfig().getIsBatchFlag();
        if(isBatchFlag != null && isBatchFlag == true) {
            log.info("batch -- start");
            if (batchCycleByHour != null && batchCycleByHour > 0) {
                List<SearchResultEntity> jobList = searchResultRepository.findByTsrIsBatch(1);

                if(jobList != null && jobList.size() > 0) {
                    scheduler = new ThreadPoolTaskScheduler();
                    scheduler.initialize();

                    for(SearchResultEntity jobItem : jobList) {
                        if(jobItem.getTsrCycleBatch() > 0 && jobItem.getMonitoringCd().equals("20")) {
                            String jobName = "job_result_" + jobItem.getTsrUno();
                            scheduler.schedule(getRunnable(jobItem.getTsrUno(), jobName), new CronTrigger(String.format("0 0 */%s * * *", jobItem.getTsrCycleBatch() + "")));
                        }
                    }
                }
            }
        }
    }

    private Runnable getRunnable(Integer tsrUno, String jobName){
        return () -> {
            log.info("job === {}, current time === {}", jobName, CommonStaticUtil.getCurrent().getSecond() + "");
            Job job = createAndScheduleJob(jobName, tsrUno);
            trackingTask(job);
        };
    }

    private Job createAndScheduleJob(String jobName, Integer tsrUno) {
        return new JobBuilder(jobName, jobRepository)
//                .start(trackingBatchConfiguration.testStep(jobRepository, transactionManager))
                .start(trackingBatchConfiguration.allTimeInfoStep(jobRepository, transactionManager, tsrUno)) // 24시간 모니터링이 체크된 리스트를 조회 후 카운트 및 시간 반영
                .next(trackingBatchConfiguration.searchInfoStep(jobRepository, transactionManager, tsrUno))   // 24시간 모니터링 수행이 한번도 되지않은 최초 데이터 리스트 조회
                .next(trackingBatchConfiguration.searchResultStep(jobRepository, transactionManager, tsrUno)) // 24시간 모니터링이 체크된 데이터를 serpApi 작업 처리 및 결과값 트랜잭션 처리
                .next(trackingBatchConfiguration.searchJobStep(jobRepository, transactionManager, tsrUno))    // 결과값 데이터를 트랜잭션 처리
                .build();
    }

    public void trackingTask(Job job) {
        JobParameters jobParameters = new JobParametersBuilder().toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }
    }
}