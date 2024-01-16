package com.nex.batch.tracking;


import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TrackingBatchConfiguration extends DefaultBatchConfiguration {
    private final AllTimeInfo allTimeInfo;
    private final SearchInfo searchInfo;
    private final SearchResult searchResult;
    private final SearchJob searchJob;
    private final int CHUNK_SIZE = 100;

    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("trackingJob 진입");

        return new JobBuilder("trackingJob", jobRepository)
                .start(allTimeInfoStep(jobRepository, transactionManager)) // 검색현황
//                .next(searchInfoMonitoringStep(jobRepository, transactionManager)) // 검색현황 시간
                // .next(allTimeMonitoringSetTimeStep(jobRepository, transactionManager)) // 마지막 모니터링 체크시간
                .next(searchInfoStep(jobRepository, transactionManager))
                .next(searchResultStep(jobRepository, transactionManager))
                .next(searchJobStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step allTimeInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("allTimeInfoStep 진입");
        return new StepBuilder("allTimeInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(allTimeInfo.allTimeInfoReader())
                .processor(allTimeInfo.allTimeInfoProcessor())
                .writer(searchInfo.searchInfoWriter())
                .build();
    }

    @Bean
    public Step searchInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchInfoStep 진입");
        return new StepBuilder("searchInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchInfo.searchInfoReader())
                .processor(searchInfo.searchInfoProcessor())
                .writer(searchInfo.searchInfoWriter())
                .build();
    }

    @Bean
    public Step searchResultStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchResultStep 진입");
        return new StepBuilder("searchResultStep", jobRepository)
                .allowStartIfComplete(true)
                .<List<YandexImagesResult>, List<SearchResultEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchResult.searchResultReader())
                .processor(searchResult.searchResultProcessor())
                .writer(searchResult.searchResultWriter())
                .build();
    }

    @Bean
    public Step searchJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchJobStep 진입");
        return new StepBuilder("searchJobStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchJobEntity >chunk(CHUNK_SIZE, transactionManager)
                .reader(searchJob.searchJobReader())
                .processor(searchJob.searchJobProcessor())
                .writer(searchJob.searchJobWriter())
                .build();
    }

/*
    @Bean
    public Step allTimeMonitoringSetTimeStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("allTimeMonitoringSetTimeStep 진입");
        return new StepBuilder("allTimeMonitoringSetTimeStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(allTimeInfoReader())
                .processor(allTimeMonitoringSetTimeProcessor())
                .writer(searchInfoWriter())
                .build();
    }
*/

/*
    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("trackingJob 진입");

        return new JobBuilder("trackingJob", jobRepository)
                .start(allTimeInfoStep(jobRepository, transactionManager))
                .build();
    }
*/


}