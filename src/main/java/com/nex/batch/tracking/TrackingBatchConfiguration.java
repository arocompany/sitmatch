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

    /* 
    Reader : tb_search_result에 monitoring_cd 값이 20인 데이터 갯수를 read
    Processor : monitoring_cd가 20인 데이터의 tsi_uno값 출력하여 기존 tsi_uno에 tsiMonitoringCnt값 update
                tb_search_info_monitoring_history에 setTsiUno, setTsimhCreateDate 삽입
    Writer : 위에 set한 값을 writer처리
    */
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

    /*
    Reader : tb_search_result 테이블에 monitoring_cd=20인 값이면서
            monitoring_cd=20인 해당 tsr_uno 값이 tb_search_info의 tsr_uno컬럼에 없는 데이터들을 read
    Processor : 새로운 tsi_uno값을 생성 후 해당 tsr_uno컬럼에  monitoring_cd=20이였던 tsr_uno 값을 update
               기존 searchResult 이미지 파일 복사 후 각 컬럼에 set
    Writer : 위에서 처리한 데이터를 write처리
    */
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

    /*
    Reader : tb_search_result의 monitoring_cd값이 20인 기존 tsruno를 갖고와서 출력 후 tb_search_info에 해당 tsruno의 이미지경로를 찾아 url을 생성 후 serpAPI에 전송 후
             json값을 받아 DB에 저장 되어 있지 않은 url 필터링하여 result를 return
    Processor : reutrn받은 값을 tb_search_result에 tsr_uno를 생성하여 set처리
    Writer : 위에서 처리한 데이터를 write처리
    */
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

    /*
    Reader : search_info에 tsrUno IS NOT NULL이면서 search_job에 해당 tsrUno값이 없는 데이터를 read
    Processor : 위에 tsrUno값이 searchJob에 없으면 tsjUno를 생성하여 set처리
    Writer : 위에서 처리한 데이터를 write처리
    */
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