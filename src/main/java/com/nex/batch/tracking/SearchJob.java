package com.nex.batch.tracking;

import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchJob {
    private final TrackingSearchJobService trackingSearchJobService;
    private final EntityManagerFactory em;
    private final int CHUNK_SIZE = 100;
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
}
