package com.nex.batch.tracking;

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

    /*
        Notice :
        Reader :
            24시간 모니터링이 체크된 리스트를 조회
        Processor :
            모니터링 된 건에 대한 실시간 데이터 반영 (테이블 삽입 및 카운트 행위)
        Writer :

     */

    @Bean
    public JpaPagingItemReader<SearchResultEntity> allTimeInfoReader() {
        log.info("allTimeInfoReader 진입");
        String queryString = """
                             select sr
                             from   SearchResultEntity sr
                             where  sr.monitoringCd = '20'
                             """;
        return new JpaPagingItemReaderBuilder<SearchResultEntity>()
                .name("allTimeInfoReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(em)
                .queryString(queryString)
                .build();
    }
    @Bean
    public ItemProcessor<SearchResultEntity, SearchInfoEntity> allTimeInfoProcessor() {
        log.info("allTimeInfoProcessor 진입");
        return findSearchResult -> {
            SearchInfoEntity findSearchInfo = searchInfoRepository.findById(findSearchResult.getTsiUno()).orElseThrow();
            return trackingSearchInfoService.getSearchInfoEntity2(findSearchInfo, findSearchResult);
        };
    }
}
