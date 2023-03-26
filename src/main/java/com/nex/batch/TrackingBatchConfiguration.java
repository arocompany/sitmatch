package com.nex.batch;

import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TrackingBatchConfiguration extends DefaultBatchConfiguration {

    private final SearchService searchService;

    private final SearchInfoRepository searchInfoRepository;

    private final SearchResultRepository searchResultRepository;

    private final SearchJobRepository searchJobRepository;


    public Job run() {
        return trackingJob(jobRepository(), getTransactionManager(), step(jobRepository(), getTransactionManager(), trackingTasklet()));
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet trackingTasklet) {
        return new StepBuilder("trackingTasklet", jobRepository)
                .tasklet(trackingTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();

    }

    @Bean
    public Tasklet trackingTasklet() {
        return new TrackingTasklet(searchService, searchInfoRepository, searchResultRepository, searchJobRepository);
    }

    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, Step step) {
        return new JobBuilder("trackingJob", jobRepository)
                .start(step)
                .build();
    }

}