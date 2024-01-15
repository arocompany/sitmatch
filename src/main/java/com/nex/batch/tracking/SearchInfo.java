package com.nex.batch.tracking;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchInfo {
    private final SearchInfoRepository searchInfoRepository;
    private final TrackingSearchInfoService trackingSearchInfoService;

    private final EntityManagerFactory em;
    private final int CHUNK_SIZE = 100;
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
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> searchInfoProcessor() {
        log.info("searchInfoProcessor 진입");
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity(findSearchInfo, findSearchResult);
        };
    }

    @Bean
    public JpaItemWriter<SearchInfoEntity> searchInfoWriter() {
        log.info("searchInfoWriter 진입");
        JpaItemWriter<SearchInfoEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(em);
        return writer;
    }
}
