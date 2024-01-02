package com.nex.batch.tracking;

import com.nex.batch.JpaItemListWriter;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchResultRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TrackingBatchConfiguration extends DefaultBatchConfiguration {

    private final EntityManagerFactory em;

    private final TrackingSearchInfoService trackingSearchInfoService;

    private final TrackingSearchResultService trackingSearchResultService;

    private final TrackingSearchJobService trackingSearchJobService;

    private final SearchInfoRepository searchInfoRepository;

    private final SearchResultRepository searchResultRepository;

    private final int CHUNK_SIZE = 100;


    //************************** searchInfo 관련 START **************************
    @Bean
    public JpaPagingItemReader<SearchResultEntity> searchInfoReader() {
        log.info("searchInfoReader 진입");
        String queryString = """
                             select sr
                             from   SearchResultEntity sr
                             where  sr.monitoringCd = '20'
                             and    exists (
                                           select 1
                                           from   SearchInfoEntity si
                                           where  si.tsiUno = sr.tsiUno
                                           )
                             and    not exists (
                                               select 1
                                               from   SearchInfoEntity si
                                               where  si.tsrUno = sr.tsrUno
                                               )
                             """;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("searchInfoReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }

    @Bean
    public JpaPagingItemReader<SearchResultEntity> allTimeInfoReader() {
        log.info("allTimeInfoReader 진입");
        String queryString = """
                             select sr
                             from   SearchResultEntity sr
                             where  sr.monitoringCd = '20'
                             and    exists (
                                           select 1
                                           from   SearchInfoEntity si
                                           where  si.tsiUno = sr.tsiUno
                                           )
                             and    not exists (
                                               select 1
                                               from   SearchInfoEntity si
                                               where  si.tsrUno = sr.tsrUno
                                               )
                             """;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("allTimeInfoReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }

    @Bean
    public JpaPagingItemReader<SearchResultEntity> searchResultAllTimeReader() {
        log.info("searchResultAllTimeReader 진입");
        String queryString = """
                             select sr
                             from   SearchResultEntity sr
                             where  sr.monitoringCd = '20'
                             and    exists (
                                           select 1
                                           from   SearchInfoEntity si
                                           where  si.tsiUno = sr.tsiUno
                                           )
                             and    not exists (
                                               select 1
                                               from   SearchInfoEntity si
                                               where  si.tsrUno = sr.tsrUno
                                               )
                             """;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("searchResultAllTimeReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }

    @Bean
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> searchInfoProcessor() {
        log.info("searchInfoProcessor 진입");
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity(findSearchInfo, findSearchResult);
        };
    }

    @Bean
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> allTimeInfoProcessor() {
        log.info("allTimeInfoProcessor 진입");
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity2(findSearchInfo, findSearchResult);
        };
    }

    @Bean
    public JpaItemWriter<SearchInfoEntity> searchInfoWriter() {
        log.info("searchInfoWriter 진입");
        JpaItemWriter<SearchInfoEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);
        return writer;
    }

    @Bean
    public Step searchInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchInfoStep 진입");
        return new StepBuilder("searchInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchInfoReader())
                .processor(searchInfoProcessor())
                .writer(searchInfoWriter())
                .build();
    }

    //************************** searchInfo 관련 END **************************

    @Bean
    public Step allTimeInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("allTimeInfoStep 진입");
        return new StepBuilder("allTimeInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(allTimeInfoReader())
                .processor(allTimeInfoProcessor())
                .writer(searchInfoWriter())
                .build();
    }

    //************************** searchResult 관련 START **************************
    @Bean
    public ItemReader<List<YandexImagesResult>> searchResultReader() {
        log.info("searchResultReader 진입");
        return new SearchResultReader(trackingSearchResultService, searchInfoRepository, searchResultRepository);
    }

    @Bean
    public ItemProcessor<List<YandexImagesResult>, List<SearchResultEntity>> searchResultProcessor() {
        log.info("searchResultProcessor 진입");
        return imagesResults -> {
            if (!imagesResults.isEmpty()) {
                //결과를 검색 결과 엔티티로 변환
                return trackingSearchResultService.resultsToSearchResultEntity(
                        imagesResults
                        , YandexImagesResult::getTsiUno
                        , YandexImagesResult::getOriginal
                        , YandexImagesResult::getThumbnail
                        , YandexImagesResult::getTitle
                        , YandexImagesResult::getLink
                        , YandexImagesResult::isFacebook
                        , YandexImagesResult::isInstagram
                );
            }

            return null;
        };
    }

    @Bean
    public JpaItemListWriter<SearchResultEntity> searchResultWriter() {
        log.info("searchResultWriter 진입");
        JpaItemWriter<SearchResultEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);

        JpaItemListWriter<SearchResultEntity> listWriter = new JpaItemListWriter<>(writer);
        listWriter.setEntityManagerFactory(em);
        return listWriter;
    }

    @Bean
    public Step searchResultStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchResultStep 진입");
        return new StepBuilder("searchResultStep", jobRepository)
                .allowStartIfComplete(true)
                .<List<YandexImagesResult>, List<SearchResultEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchResultReader())
                .processor(searchResultProcessor())
                .writer(searchResultWriter())
                .build();
    }
    //************************** searchResult 관련 END **************************


    //************************** searchJob 관련 START **************************
    @Bean
    public ItemReader<SearchResultEntity> searchJobReader() {
        log.info("searchJobReader 진입");
        String queryString = """
                             select sr
                             from   SearchResultEntity sr
                             inner  join SearchInfoEntity si
                                    on   si.tsiUno = sr.tsiUno
                                    and  si.tsrUno is not null
                             where  not exists (
                                               select 1
                                               from   SearchJobEntity sj
                                               where  sj.tsrUno = sr.tsrUno
                                               )
                             """;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("searchJobReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }

    @Bean
    public ItemProcessor<SearchResultEntity, SearchJobEntity> searchJobProcessor() {
        log.info("searchJobProcessor 진입");
        return trackingSearchJobService::searchResultEntityToSearchJobEntity;
    }

    @Bean
    public JpaItemWriter<SearchJobEntity> searchJobWriter() {
        log.info("searchJobWriter 진입");
        JpaItemWriter<SearchJobEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);
        return writer;
    }

    @Bean
    public Step searchJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("searchJobStep 진입");
        return new StepBuilder("searchJobStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchJobEntity >chunk(CHUNK_SIZE, transactionManager)
                .reader(searchJobReader())
                .processor(searchJobProcessor())
                .writer(searchJobWriter())
                .build();
    }
    //************************** searchJob 관련 START **************************

    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("trackingJob 진입");

        return new JobBuilder("trackingJob", jobRepository)
                .start(allTimeInfoStep(jobRepository, transactionManager))
                .build();
    }


/*
    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("trackingJob 진입");

        return new JobBuilder("trackingJob", jobRepository)
                .start(allTimeInfoStep(jobRepository, transactionManager))
                .next(searchInfoStep(jobRepository, transactionManager))
                .next(searchResultStep(jobRepository, transactionManager))
                .next(searchJobStep(jobRepository, transactionManager))
                .build();
    }
*/


    /*
    @Bean
    public Job trackingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("trackingJob 진입");
        return new JobBuilder("trackingJob", jobRepository)
                .start(searchInfoStep(jobRepository, transactionManager))
                .next(searchResultStep(jobRepository, transactionManager))
                .next(searchJobStep(jobRepository, transactionManager))
                .build();
    }
    */
}