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

        /*
        Notice : 꼭 명시해야할 것
                Table to Table / 어떤 테이블에 어떤 행을 몇개?를 리스트업 하여,
                               / 어떤 테이블에 어떻게 행을 삽입 또는 Update하는지?
                                                        Update한다면,,??
                                                         어떤 테이블에 어떤 키값을 가진 행을?

        Reader : tb_search_result에 monitoring_cd=20인 값이면서
                 monitoring_cd=20인 해당 tsr_uno 값이 tb_search_info의 tsr_uno컬럼에 없는 데이터들을 read
        Processor : 새로운 tsi_uno값을 생성 후 해당 tsr_uno컬럼에  monitoring_cd=20이였던 tsr_uno 값을 삽입
                    기존 searchResult 이미지 파일 복사 후 각 컬럼에 set
        Writer : 위에 set한 데이터를 write처리
        */

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
