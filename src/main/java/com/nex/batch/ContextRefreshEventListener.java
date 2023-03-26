package com.nex.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


@Slf4j
@Component
public class ContextRefreshEventListener implements ApplicationListener<ContextRefreshedEvent> {
    private final JobExplorer jobExplorer;
    private final JobRepository jobRepository;

    public ContextRefreshEventListener(JobExplorer jobExplorer, JobRepository jobRepository) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
    }

    // Spring 이 기동될 때마다 수행됨. 상태가 STARTED인 Job 전체 조회 후 상태를 FAILD 처리
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("Container Start. find STARTED STATUS Job and Change to FAILED");
        List<String> jobs = jobExplorer.getJobNames();
        for (String job : jobs) {
            Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(job);

            for (JobExecution runningJob : runningJobs) {
                try {
                    if (!runningJob.getStepExecutions().isEmpty()) {
                        Iterator<StepExecution> iter = runningJob.getStepExecutions().iterator();
                        while (iter.hasNext()) {
                            StepExecution runningStep = iter.next();
                            if (runningStep.getStatus().isRunning()) {
                                runningStep.setEndTime(LocalDateTime.now());
                                runningStep.setStatus(BatchStatus.FAILED);
                                runningStep.setExitStatus(new ExitStatus("FAILED", "BATCH FAILED"));
                                jobRepository.update(runningStep);
                            }
                        }
                    }
                    runningJob.setEndTime(LocalDateTime.now());
                    runningJob.setStatus(BatchStatus.FAILED);
                    runningJob.setExitStatus(new ExitStatus("FAILED", "BATCH FAILED"));
                    jobRepository.update(runningJob);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}