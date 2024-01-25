package com.nex.batch.tracking.step;

import com.nex.batch.tracking.TrackingSearchJobService;
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

    public ItemReader<SearchResultEntity> searchJobReader(Integer tsrUno) {
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
                             and sr.tsrUno = """ + tsrUno;
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
}
