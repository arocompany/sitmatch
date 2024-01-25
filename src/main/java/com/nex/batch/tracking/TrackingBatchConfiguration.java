package com.nex.batch.tracking;


import com.nex.batch.tracking.step.AllTimeInfo;
import com.nex.batch.tracking.step.SearchInfo;
import com.nex.batch.tracking.step.SearchJob;
import com.nex.batch.tracking.step.SearchResult;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

//    @Bean
//    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new JobBuilder("trackingJob", jobRepository)
//                .start(allTimeInfoStep(jobRepository, transactionManager)) // 24시간 모니터링이 체크된 리스트를 조회 후 카운트 및 시간 반영
//                .next(searchInfoStep(jobRepository, transactionManager))   // 24시간 모니터링 수행이 한번도 되지않은 최초 데이터 리스트 조회
//                .next(searchResultStep(jobRepository, transactionManager)) // 24시간 모니터링이 체크된 데이터를 serpApi 작업 처리 및 결과값 트랜잭션 처리
//                .next(searchJobStep(jobRepository, transactionManager))    // 결과값 데이터를 트랜잭션 처리
//                .build();
//    }
    public Step allTimeInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Integer tsrUno) {
        return new StepBuilder("allTimeInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(allTimeInfo.allTimeInfoReader(tsrUno))
                .processor(allTimeInfo.allTimeInfoProcessor())
                .writer(searchInfo.searchInfoWriter())
                .build();
    }


    public Step searchInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Integer tsrUno) {
        return new StepBuilder("searchInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchInfo.searchInfoReader(tsrUno))
                .processor(searchInfo.searchInfoProcessor())
                .writer(searchInfo.searchInfoWriter())
                .build();
    }


    public Step searchResultStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Integer tsrUno) {
        return new StepBuilder("searchResultStep", jobRepository)
                .allowStartIfComplete(true)
                .<List<ImagesResult>, List<SearchResultEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchResult.searchResultReader(tsrUno))
                .processor(searchResult.searchResultProcessor())
                .writer(searchResult.searchResultWriter())
                .build();
    }


    public Step searchJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Integer tsrUno) {
        return new StepBuilder("searchJobStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchJobEntity >chunk(CHUNK_SIZE, transactionManager)
                .reader(searchJob.searchJobReader(tsrUno))
                .processor(searchJob.searchJobProcessor())
                .writer(searchJob.searchJobWriter())
                .build();
    }
}