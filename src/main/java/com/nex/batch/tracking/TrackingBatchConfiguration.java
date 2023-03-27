package com.nex.batch.tracking;

import com.nex.batch.JpaItemListWriter;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
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

    private final SearchJobRepository searchJobRepository;

    private final int CHUNK_SIZE = 100;


    //************************** searchInfo 관련 START **************************
    @Bean
    public JpaPagingItemReader<SearchResultEntity> searchInfoReader() {
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
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> searchInfoProcessor() {
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity(findSearchInfo, findSearchResult);
        };
    }

    @Bean
    public JpaItemWriter<SearchInfoEntity> searchInfoWriter() {
        JpaItemWriter<SearchInfoEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);
        return writer;
    }

    @Bean
    public Step searchInfoStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("searchInfoStep", jobRepository)
                .allowStartIfComplete(true)
                .<SearchResultEntity, SearchInfoEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(searchInfoReader())
                .processor(searchInfoProcessor())
                .writer(searchInfoWriter())
                .build();
    }
    //************************** searchInfo 관련 END **************************


    //************************** searchResult 관련 START **************************
    @Bean
    public ItemReader<List<YandexImagesResult>> searchResultReader() {
        return new SearchResultReader(trackingSearchResultService, searchInfoRepository, searchResultRepository);
    }

    @Bean
    public ItemProcessor<List<YandexImagesResult>, List<SearchResultEntity>> searchResultProcessor() {
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
        JpaItemWriter<SearchResultEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);

        JpaItemListWriter<SearchResultEntity> listWriter = new JpaItemListWriter<>(writer);
        listWriter.setEntityManagerFactory(em);
        return listWriter;
    }

    @Bean
    public Step searchResultStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
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
        return trackingSearchJobService::searchResultEntityToSearchJobEntity;
    }

    @Bean
    public JpaItemWriter<SearchJobEntity> searchJobWriter() {
        JpaItemWriter<SearchJobEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);
        return writer;
    }

    @Bean
    public Step searchJobStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
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
        return new JobBuilder("trackingJob", jobRepository)
                .start(searchInfoStep(jobRepository, transactionManager))
                .next(searchResultStep(jobRepository, transactionManager))
                .next(searchJobStep(jobRepository, transactionManager))
                .build();
    }

}