package com.nex.batch.tracking.step;

import com.nex.batch.tracking.TrackingSearchInfoService;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AllTimeInfo {
    private final SearchInfoRepository searchInfoRepository;
    private final TrackingSearchInfoService trackingSearchInfoService;

    private final EntityManagerFactory em;
    private final int CHUNK_SIZE = 100;

    public JpaPagingItemReader<SearchResultEntity> allTimeInfoReader(Integer tsrUno) {
        String queryString = "select sr from   SearchResultEntity sr where  sr.monitoringCd = '20' and sr.tsrUno = "+ tsrUno;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("allTimeInfoReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }
    @Bean
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> allTimeInfoProcessor() {
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity2(findSearchInfo, findSearchResult);
        };
    }
}
