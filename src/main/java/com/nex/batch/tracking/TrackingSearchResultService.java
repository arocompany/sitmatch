package com.nex.batch.tracking;

import com.nex.common.Consts;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingSearchResultService{

    private final SearchService searchService;

    @Value("${search.yandex.text.url}")
    private String textYandexUrl;

    @Value("${search.yandex.text.gl}")
    private String textYandexGl;

    @Value("${search.yandex.text.no_cache}")
    private String textYandexNocache;

    @Value("${search.yandex.text.location}")
    private String textYandexLocation;

    @Value("${search.yandex.text.tbm}")
    private String textYandexTbm;

    @Value("${search.yandex.text.api_key}")
    private String textYandexApikey;

    @Value("${search.yandex.text.engine}")
    private String textYandexEngine;

    @Value("${search.yandex.image.engine}")
    private String imageYandexEngine;

    @Value("${search.yandex.text.count.limit}")
    private Integer textYandexCountLimit;

    @Value("${search.yandex.text.count.page}")
    private Integer textYandexCountPage;

    @Value("${file.location1}")
    private String fileLocation1;

    @Value("${file.location3}")
    private String fileLocation3;

    @Value("${server.url}")
    private String serverIp;


    /**
     * 모든 결과 List 추출
     *
     * @param  searchInfoEntity (검색 이력 엔티티)
     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
     * @param  getErrorFn       (info error getter Function)
     * @param  getSubFn         (RESULT getter Function)
     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    public  <INFO, RESULT> List<RESULT> getAllResults(SearchInfoEntity searchInfoEntity
            , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Boolean isText) {
        log.info("모든 결과 List 추출 getAllResults 진입");
        List<String> siteUrls = searchService.findTsrSiteUrlDistinctByTsiUno(searchInfoEntity.getTsiUno());

        String tsiKeyword = searchInfoEntity.getTsiKeyword();

        //모든 결과 List
        List<RESULT> allResults = new ArrayList<>();

        //구글 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiGoogle())) {
            //모든 결과 List 에 add
            allResults.addAll(
                    //결과 List 추출
                    getResults(
                            tsiKeyword
                            , searchInfoEntity
                            , Consts.GOOGLE
                            , siteUrls
                            , infoClass
                            , getErrorFn
                            , getSubFn
                            , setTsiUnoCn
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
                            , isText
                    )
            );
        }
        //페이스북 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiFacebook())) {
            //모든 결과 List 에 add
            allResults.addAll(
                    //결과 List 추출
                    getResults(
                            "페이스북 " + tsiKeyword
                            , searchInfoEntity
                            , Consts.FACEBOOK
                            , siteUrls
                            , infoClass
                            , getErrorFn
                            , getSubFn
                            , setTsiUnoCn
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
                            , isText
                    )
            );
        }
        //인스타그램 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiInstagram())) {
            //모든 결과 List 에 add
            allResults.addAll(
                    //결과 List 추출
                    getResults(
                            "인스타그램 " + tsiKeyword
                            , searchInfoEntity
                            , Consts.INSTAGRAM
                            , siteUrls
                            , infoClass
                            , getErrorFn
                            , getSubFn
                            , setTsiUnoCn
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
                            , isText
                    )
            );
        }

        return allResults;
    }

    /**
     * 결과 List 추출
     *
     * @param  tsiKeyword       (검색어)
     * @param  searchInfoEntity (검색 정보 엔티티)
     * @param  dvn              (구분)
     * @param  siteUrls         (사이트 URL List)
     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
     * @param  getErrorFn       (info error getter Function)
     * @param  getSubFn         (RESULT getter Function)
     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
            , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Boolean isText) {
        log.info("결과 List 추출 getResults 진입");
        List<RESULT> results = new ArrayList<>();


        return getResults(
                tsiKeyword
                , searchInfoEntity
                , dvn
                , siteUrls
                , results
                , 0
                , infoClass
                , getErrorFn
                , getSubFn
                , setTsiUnoCn
                , getLinkFn
                , isFacebookFn
                , isInstagram
                , isText
        );
    }


    /**
     * 이미지 결과 목록 추출
     *
     * @param  tsiKeyword       (검색어)
     * @param  searchInfoEntity (검색 정보 엔티티)
     * @param  dvn              (구분)
     * @param  siteUrls         (사이트 URL List)
     * @param  results          (결과 List)
     * @param  index            (인덱스)
     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
     * @param  getErrorFn       (info error getter Function)
     * @param  getSubFn         (RESULT getter Function)
     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
            , List<RESULT> results, int index, Class<INFO> infoClass, Function<INFO, String> getErrorFn
            , Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Boolean isText) {
        log.info("이미지 결과 목록 추출 getResults 진입, index : "+index);
        boolean isFirst = index == 0;
        boolean loop = true;

        List<CompletableFuture<List<RESULT>>> completableFutures = new ArrayList<>();

        //기존 SearchService 에 있던 부분 활용
        do {
            String url = getUrl(tsiKeyword, index, isText, searchInfoEntity);
            log.info(" ### url ### : {} ", url);

            CompletableFuture<List<RESULT>> listCompletableFuture = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            // text기반 yandex 검색
                            return searchService.searchYandex(url, infoClass, getErrorFn, getSubFn);
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                        }
                        return null;
                    });

            completableFutures.add(listCompletableFuture);

            if (index >= textYandexCountLimit - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        //결과 값을 받아온다.
        List<RESULT> searchResults = completableFutures.stream().map(CompletableFuture::join).flatMap(s -> s.stream()).toList();

        int size = searchResults.size();

        //페이스북
        if (Consts.FACEBOOK.equals(dvn)) {
            //페이스북으로 필터
            searchResults = searchResults.stream().filter(searchResult -> isFacebookFn.apply(searchResult)).toList();
        }
        //인스타그램
        if (Consts.INSTAGRAM.equals(dvn)) {
            //인스타그램으로 필터
            searchResults = searchResults.stream().filter(searchResult -> isInstagram.apply(searchResult)).toList();
        }

        //DB에 저장 되어 있지 않은 url 필터
        searchResults = searchResults.stream().filter(searchResult -> !siteUrls.contains(getLinkFn.apply(searchResult))).toList();

        searchResults.forEach(searchResult -> setTsiUnoCn.accept(searchResult, searchInfoEntity.getTsiUno()));

        //return 값에 담는다.
        results.addAll(searchResults);

        //다음 페이지 조회 가능 여부
        //첫 검색시 결과가 모든 페이지에 100개 씩 있을 경우 or 첫 검색이 아니고 페이지에 100개 결과가 있을 경우
        boolean isNextAble = (isFirst && size == index * 100) || (!isFirst && size == 100);

        //결과 size 가 textYandexCountLimit * 100 보다 작고, 다음 페이지 조회 가능 여부가 true 이면 재검색
        if (textYandexCountLimit * textYandexCountPage > results.size() && isNextAble) {
            return getResults(
                    tsiKeyword
                    , searchInfoEntity
                    , dvn
                    , siteUrls
                    , results
                    , index
                    , infoClass
                    , getErrorFn
                    , getSubFn
                    , setTsiUnoCn
                    , getLinkFn
                    , isFacebookFn
                    , isInstagram
                    , isText
            );
        }

        //결과 size 가 textYandexCountLimit * textYandexCountPage 보다 크면 textYandexCountLimit * textYandexCountPage 만큼만 잘라낸다.
        if (results.size() > textYandexCountLimit * textYandexCountPage) {
            results = results.subList(0, textYandexCountLimit * textYandexCountPage);
        }

        return results;
    }

    /**
     * URL 추출
     *
     * @param  tsiKeyword       (검색어)
     * @param  index            (인덱스)
     * @param  isText           (텍스트 검색 여부)
     * @param  searchInfoEntity (검색 정보 엔티티)
     * @return String           (URL)
     */
    private String getUrl(String tsiKeyword, int index, Boolean isText, SearchInfoEntity searchInfoEntity) {
        log.info("getUrl 진입");
        String url;

        //텍스트 검색
        if (isText) {
            // yandex search url
            url = textYandexUrl
                    + "?q=" + tsiKeyword
                    + "&gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&tbm=" + textYandexTbm
                    + "&ijn=" + index
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&api_key=" + textYandexApikey
                    + "&engine=" + textYandexEngine;
        }
        //이미지 검색
        else {
            String searchImageUrl = searchInfoEntity.getTsiImgPath() + searchInfoEntity.getTsiImgName();
            searchImageUrl = serverIp + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);

            url = textYandexUrl
                    + "&gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&engine=" + imageYandexEngine
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&image_url=" + searchImageUrl;
        }
        log.info("url : "+url);

        return url;
    }

    /**
     * 결과를 검색 결과 엔티티로 변환
     *
     * @param  results                  (결과 List)
     * @param  getTsiUnoFn              (검색 정보 테이블의 key getter Function)
     * @param  getOriginalFn            (original getter Function)
     * @param  getThumbnailFn           (thumbnail getter Function)
     * @param  getTitleFn               (title getter Function)
     * @param  getLinkFn                (link getter Function)
     * @param  isFacebookFn             (isFacebook Function)
     * @param  isInstagramFn            (isInstagram Function)
     * @return List<SearchResultEntity> (검색 결과 엔티티 List)
     * @param  <RESULT>                 (결과)
     */
    public <RESULT> List<SearchResultEntity> resultsToSearchResultEntity(List<RESULT> results, Function<RESULT, Integer> getTsiUnoFn
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) {
        List<CompletableFuture<SearchResultEntity>> completableFutures = new ArrayList<>();

        for (RESULT result : results) {
            CompletableFuture<SearchResultEntity> completableFuture = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            String tsrSns;                                      //SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북)
                            RestTemplate restTemplate = new RestTemplate();     //RestTemplate

                            //페이스북
                            if (isFacebookFn.apply(result)) {
                                tsrSns = "17";
                            }
                            //인스타그램
                            else if (isInstagramFn.apply(result)) {
                                tsrSns = "15";
                            }
                            //구글
                            else {
                                tsrSns = "11";
                            }

                            //검색 결과 엔티티 추출
                            SearchResultEntity searchResultEntity = searchService.getSearchResultEntity(getTsiUnoFn.apply(result), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                            try {
                                //이미지 파일 저장
                                searchService.saveImageFile(getTsiUnoFn.apply(result), restTemplate, searchResultEntity, result, getOriginalFn, getThumbnailFn);
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                                throw new RuntimeException(e);
                            }

                            //검색 결과 엔티티 기본값 세팅
                            searchService.setSearchResultDefault(searchResultEntity);

                            return searchResultEntity;
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                        }
                        return null;
                    });

            completableFutures.add(completableFuture);
        }

        //결과 값을 받아온다.
        List<SearchResultEntity> searchResults = completableFutures.stream().map(CompletableFuture::join).toList();

        return searchResults;
    }

}