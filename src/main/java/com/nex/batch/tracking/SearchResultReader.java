package com.nex.batch.tracking;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SearchResultReader implements ItemReader<List<YandexImagesResult>> {

    private final TrackingSearchResultService trackingSearchResultService;

    private final SearchInfoRepository searchInfoRepository;

    private final SearchResultRepository searchResultRepository;

    public SearchResultReader(TrackingSearchResultService trackingSearchResultService, SearchInfoRepository searchInfoRepository, SearchResultRepository searchResultRepository) {
        this.trackingSearchResultService = trackingSearchResultService;
        this.searchInfoRepository = searchInfoRepository;
        this.searchResultRepository = searchResultRepository;
    }

    private int page = 0;


    @Override
    public List<YandexImagesResult> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("read() 진입");
        log.info("page1: "+page);

        //한번만 실행
        if (page > 0) {
            page=0;
            log.info("page2: "+page);
            return null;
        }
        log.info("read() 진입2");

        List<YandexImagesResult> results = new ArrayList<>();

        //24시간 모니터링 코드 값이 20 (모니터링) 인 데이터 조회
        List<SearchResultEntity> dtoList = searchResultRepository.findByMonitoringCd("20");

        //검색 정보 테이블의 PK List
        List<Integer> tsiUnoList = dtoList.stream().map(SearchResultEntity::getTsiUno).toList();
        //검색 결과 테이블의 PK List
        List<Integer> tsrUnoList = dtoList.stream().map(SearchResultEntity::getTsrUno).toList();

        //검색 정보 테이블의 PK 로 조회
        List<SearchInfoEntity> searchInfoEntitiesByTsiUno = searchInfoRepository.findByTsiUnoIn(tsiUnoList);
        //검색 결과 테이블의 PK 로 조회
        List<SearchInfoEntity> searchInfoEntitiesByTsrUno = searchInfoRepository.findByTsrUnoIn(tsrUnoList);

        //검색 정보 테이블의 PK 를 key, 검색 정보 엔티티를 value 로 Map 을 만든다.
        Map<Integer, SearchInfoEntity> searchInfoEntityMapByTsiUno = searchInfoEntitiesByTsiUno.stream().collect(Collectors.toMap(SearchInfoEntity::getTsiUno, Function.identity()));
        //검색 결과 테이블의 PK 를 key, 검색 정보 엔티티를 value 로 Map 을 만든다.
        Map<Integer, SearchInfoEntity> searchInfoEntityMapByTsrUno = searchInfoEntitiesByTsrUno.stream().collect(Collectors.toMap(SearchInfoEntity::getTsrUno, Function.identity()));

        SearchInfoEntity searchInfoEntityByTsiUno;
        SearchInfoEntity searchInfoEntityByTsrUno;

        boolean isNotImage;
        for (SearchResultEntity searchResultEntity : dtoList) {
            log.info(" ### searchResultEntity ### : {}", searchResultEntity);

            searchInfoEntityByTsiUno = searchInfoEntityMapByTsiUno.get(searchResultEntity.getTsiUno());
            searchInfoEntityByTsrUno = searchInfoEntityMapByTsrUno.get(searchResultEntity.getTsrUno());

            //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            isNotImage = !"17".equals(searchInfoEntityByTsiUno.getTsiType());

            //모든 결과 List 추출
            List<YandexImagesResult> allResults = trackingSearchResultService.getAllResults(
                    searchInfoEntityByTsrUno
                    , YandexResult.class
                    , YandexResult::getError
                    , isNotImage ? YandexResult::getImages_results : YandexResult::getInline_images
                    , YandexImagesResult::setTsiUno
                    , YandexImagesResult::getLink
                    , YandexImagesResult::isFacebook
                    , YandexImagesResult::isInstagram
                    , isNotImage
            );

            log.info("allResults: " + allResults);

            results.addAll(allResults);
        }

        page++;

        return results;
    }

}