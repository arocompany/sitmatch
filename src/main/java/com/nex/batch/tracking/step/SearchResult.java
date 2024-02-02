package com.nex.batch.tracking.step;

import com.nex.batch.JpaItemListWriter;
import com.nex.batch.tracking.ImagesResult;
import com.nex.batch.tracking.SerpApiImageResult;
import com.nex.batch.tracking.TrackingSearchResultService;
import com.nex.common.CommonCode;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchResultRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.*;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchResult implements ItemReader<List<ImagesResult>> {
    private final TrackingSearchResultService trackingSearchResultService;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final EntityManagerFactory em;
    private int page = 0;

    private Integer tsrUno = 0;

    @Override
    public List<ImagesResult> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        //한번만 실행
        if (page > 0) {
            page=0;
            log.info("page2: "+page);
            return null;
        }


        List<ImagesResult> results = new ArrayList<>();

        SearchResultEntity dtoList = searchResultRepository.findByTsrUno(tsrUno);

        List<Integer> tsrUnoList = new ArrayList<>();
        tsrUnoList.add(dtoList.getTsrUno());

        //검색 결과 테이블의 PK 로 조회
        List<SearchInfoEntity> searchInfoEntitiesByTsrUno = searchInfoRepository.findByTsrUnoIn(tsrUnoList);

        SearchInfoEntity searchInfoEntityByTsiUno = searchInfoRepository.findByTsrUno(dtoList.getTsrUno());
        Map<Integer, SearchInfoEntity> searchInfoEntityMapByTsrUno = searchInfoEntitiesByTsrUno.stream().collect(Collectors.toMap(SearchInfoEntity::getTsrUno, Function.identity()));

//        for (SearchResultEntity searchResultEntity : dtoList) {
//            log.info(" ### searchResultEntity ### : {}", dtoList);

//            searchInfoEntityByTsiUno = searchInfoEntityMapByTsiUno.get(searchResultEntity.getTsiUno());
//            searchInfoEntityByTsrUno = searchInfoEntityMapByTsrUno.get(searchResultEntity.getTsrUno());
        SearchInfoEntity searchInfoEntityByTsrUno = searchInfoEntityMapByTsrUno.get(dtoList.getTsrUno());

            //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
        boolean isNotImage = !CommonCode.searchTypeImage.equals(searchInfoEntityByTsiUno.getTsiType())
                    && !CommonCode.searchTypeVideo.equals(searchInfoEntityByTsiUno.getTsiType());

            //모든 결과 List 추출
            List<ImagesResult> allResults = trackingSearchResultService.getAllResults(
                    searchInfoEntityByTsrUno
                    , SerpApiImageResult.class
                    , SerpApiImageResult::getError
                    , isNotImage ? SerpApiImageResult::getImages_results : SerpApiImageResult::getInline_images
                    , ImagesResult::setTsiUno
                    , ImagesResult::getLink
                    , ImagesResult::isFacebook
                    , ImagesResult::isInstagram
                    , ImagesResult::isTwitter
                    , isNotImage
            );

//            log.info("allResults: " + allResults);

            results.addAll(allResults);
//        }

        page++;

        return results;
    }


//    public List<ImagesResult> read2() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//        log.info("read() 진입");
//        log.info("page1: "+page);
//
//        //한번만 실행
//        if (page > 0) {
//            page=0;
//            log.info("page2: "+page);
//            return null;
//        }
//        log.info("read() 진입2");
//
//        List<ImagesResult> results = new ArrayList<>();
//
//        //24시간 모니터링 코드 값이 20 (모니터링) 인 데이터 조회
//        List<SearchResultEntity> dtoList = searchResultRepository.findByMonitoringCd("20");
//
//
//
//        //검색 정보 테이블의 PK List
//        List<Integer> tsiUnoList = dtoList.stream().map(SearchResultEntity::getTsiUno).toList();
//        //검색 결과 테이블의 PK List
//        List<Integer> tsrUnoList = dtoList.stream().map(SearchResultEntity::getTsrUno).toList();
//
//        //검색 정보 테이블의 PK 로 조회
//        List<SearchInfoEntity> searchInfoEntitiesByTsiUno = searchInfoRepository.findByTsiUnoIn(tsiUnoList);
//        //검색 결과 테이블의 PK 로 조회
//        List<SearchInfoEntity> searchInfoEntitiesByTsrUno = searchInfoRepository.findByTsrUnoIn(tsrUnoList);
//
//        //검색 정보 테이블의 PK 를 key, 검색 정보 엔티티를 value 로 Map 을 만든다.
//        Map<Integer, SearchInfoEntity> searchInfoEntityMapByTsiUno = searchInfoEntitiesByTsiUno.stream().collect(Collectors.toMap(SearchInfoEntity::getTsiUno, Function.identity()));
//        //검색 결과 테이블의 PK 를 key, 검색 정보 엔티티를 value 로 Map 을 만든다.
//        Map<Integer, SearchInfoEntity> searchInfoEntityMapByTsrUno = searchInfoEntitiesByTsrUno.stream().collect(Collectors.toMap(SearchInfoEntity::getTsrUno, Function.identity()));
//
//        SearchInfoEntity searchInfoEntityByTsiUno;
//        SearchInfoEntity searchInfoEntityByTsrUno;
//
//        boolean isNotImage;
//        for (SearchResultEntity searchResultEntity : dtoList) {
//            log.info(" ### searchResultEntity ### : {}", searchResultEntity);
//
//            searchInfoEntityByTsiUno = searchInfoEntityMapByTsiUno.get(searchResultEntity.getTsiUno());
//            searchInfoEntityByTsrUno = searchInfoEntityMapByTsrUno.get(searchResultEntity.getTsrUno());
//
//            //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
//            isNotImage = !CommonCode.searchTypeImage.equals(searchInfoEntityByTsiUno.getTsiType())
//                    && !CommonCode.searchTypeVideo.equals(searchInfoEntityByTsiUno.getTsiType());
//
//            //모든 결과 List 추출
//            List<ImagesResult> allResults = trackingSearchResultService.getAllResults(
//                    searchInfoEntityByTsrUno
//                    , SerpApiImageResult.class
//                    , SerpApiImageResult::getError
//                    , isNotImage ? SerpApiImageResult::getImages_results : SerpApiImageResult::getInline_images
//                    , ImagesResult::setTsiUno
//                    , ImagesResult::getLink
//                    , ImagesResult::isFacebook
//                    , ImagesResult::isInstagram
//                    , isNotImage
//            );
//
//            log.info("allResults: " + allResults);
//
//            results.addAll(allResults);
//        }
//
//        page++;
//
//        return results;
//    }


    public ItemReader<List<ImagesResult>> searchResultReader(Integer tsrUno) {
        this.tsrUno = tsrUno;
        return this;
    }
    @Bean
    public ItemProcessor<List<ImagesResult>, List<SearchResultEntity>> searchResultProcessor() {
        return imagesResults -> {
            if (!imagesResults.isEmpty()) {
                //결과를 검색 결과 엔티티로 변환
                return trackingSearchResultService.resultsToSearchResultEntity(
                        imagesResults
                        , ImagesResult::getTsiUno
                        , ImagesResult::getOriginal
                        , ImagesResult::getThumbnail
                        , ImagesResult::getTitle
                        , ImagesResult::getLink
                        , ImagesResult::isFacebook
                        , ImagesResult::isInstagram
                        , ImagesResult::isTwitter
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
}
