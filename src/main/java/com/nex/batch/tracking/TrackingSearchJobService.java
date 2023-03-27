package com.nex.batch.tracking;

import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingSearchJobService {

    private final SearchService searchService;


    /**
     * 검색 결과 엔티티를 검색 작업 엔티티로 변환
     *
     * @param  searchResultEntity (검색 결과 엔티티)
     * @return SearchJobEntity    (검색 작업 엔티티)
     */
    public SearchJobEntity searchResultEntityToSearchJobEntity(SearchResultEntity searchResultEntity) {
        //검색 작업 엔티티 추출
        SearchJobEntity searchJobEntity = searchService.getSearchJobEntity(searchResultEntity);

        //검색 작업 엔티티 기본값 세팅
        searchService.setSearchJobDefault(searchJobEntity);

        return searchJobEntity;
    }

}