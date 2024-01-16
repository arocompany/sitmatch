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
        Notice : 꼭 명시해야할 것
                Table to Table / 어떤 테이블에 어떤 행을 몇개?를 리스트업 하여,
                               / 어떤 테이블에 어떻게 행을 삽입 또는 Update하는지?
                                                        Update한다면,,??
                                                         어떤 테이블에 어떤 키값을 가진 행을?

        Reader : tb_search_result에 monitoring_cd 값이 20인 데이터 갯수를 read
        Processor : monitoring_cd가 20인 데이터의 tsi_uno값 출력하여 기존 tsi_uno에 tsiMonitoringCnt값 삽입
                    tb_search_info_monitoring_history에 setTsiUno, setTsimhCreateDate 삽입

        Writer : 위에 set한 값을 writer처리
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
