package com.nex.batch;

import com.nex.common.Consts;
import com.nex.search.entity.*;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class TrackingTasklet implements Tasklet {

    private final SearchService searchService;

    private final SearchInfoRepository searchInfoRepository;

    private final SearchResultRepository searchResultRepository;

    private final SearchJobRepository searchJobRepository;

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

    private String searchImageUrl;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        log.info(" ### TrackingTasklet.execute() ### ");

        //24시간 모니터링 코드 값이 20 (모니터링) 인 데이터 조회
        List<SearchResultEntity> dtoList = searchService.findByMonitoringCd("20");

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

        List<SearchResultEntity> searchResultEntities;
        for (SearchResultEntity searchResultEntity : dtoList) {
            log.info(" ### searchResultEntity ### : {}", searchResultEntity);

            searchResultEntities = null;

            //검색 정보 테이블의 PK 값으로 저장된 데이터가 없을 경우
            if (!searchInfoEntityMapByTsiUno.containsKey(searchResultEntity.getTsiUno())) {
                continue;
            }

            searchInfoEntityByTsiUno = searchInfoEntityMapByTsiUno.get(searchResultEntity.getTsiUno());


            //검색 결과 테이블의 PK 값으로 저장된 데이터가 없을 경우
            if (!searchInfoEntityMapByTsrUno.containsKey(searchResultEntity.getTsrUno())) {
                //검색 정보 엔티티 추출
                SearchInfoEntity searchInfoEntity = getSearchInfoEntity(searchInfoEntityByTsiUno, searchResultEntity);

                //검색 정보 저장
                searchInfoEntityByTsrUno = searchInfoRepository.save(searchInfoEntity);
            }
            else {
                searchInfoEntityByTsrUno = searchInfoEntityMapByTsrUno.get(searchResultEntity.getTsrUno());
            }

            
            //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            //이미지 검색인 경우
            if ("17".equals(searchInfoEntityByTsiUno.getTsiType())) {
                //모든 결과 List 추출
                List<Images_resultsByImage> allResults = getAllResults(
                        searchInfoEntityByTsrUno
                        , YandexByImageResult.class
                        , YandexByImageResult::getError
                        , YandexByImageResult::getInline_images
                        , Images_resultsByImage::getLink
                        , Images_resultsByImage::isFacebook
                        , Images_resultsByImage::isInstagram
                );

                //결과 List가 있을 경우
                if (!allResults.isEmpty()) {
                    //결과를 검색 결과 엔티티로 변환
                    searchResultEntities = resultsToSearchResultEntity(
                            searchResultEntity.getTsiUno()
                            , allResults
                            , Images_resultsByImage::getOriginal
                            , Images_resultsByImage::getThumbnail
                            , Images_resultsByImage::getTitle
                            , Images_resultsByImage::getLink
                            , Images_resultsByImage::isFacebook
                            , Images_resultsByImage::isInstagram
                    );
                }
            }
            //이미지 검색이 아닌경우
            else {
                //모든 결과 List 추출
                List<Images_resultsByText> allResults = getAllResults(
                        searchInfoEntityByTsrUno
                        , YandexByTextResult.class
                        , YandexByTextResult::getError
                        , YandexByTextResult::getImages_results
                        , Images_resultsByText::getLink
                        , Images_resultsByText::isFacebook
                        , Images_resultsByText::isInstagram
                );

                //결과 List가 있을 경우
                if (!allResults.isEmpty()) {
                    //결과를 검색 결과 엔티티로 변환
                    searchResultEntities = resultsToSearchResultEntity(
                            searchResultEntity.getTsiUno()
                            , allResults
                            , Images_resultsByText::getOriginal
                            , Images_resultsByText::getThumbnail
                            , Images_resultsByText::getTitle
                            , Images_resultsByText::getLink
                            , Images_resultsByText::isFacebook
                            , Images_resultsByText::isInstagram
                    );
                }
            }

            //검색 결과 엔티티 List 가 있으면
            if (searchResultEntities != null && !searchResultEntities.isEmpty()) {
                log.info(" ### searchResultEntities.size() ### : {} ", searchResultEntities.size());
                //검색 결과 List 저장
                List<SearchResultEntity> saveSearchResultEntities = searchResultRepository.saveAll(searchResultEntities);

                //검색 결과 엔티티를 검색 작업 엔티티로 변환
                List<SearchJobEntity> searchJobEntities = searchResultEntityToSearchJobEntity(saveSearchResultEntities);

                //검색 작업 List 저장
                searchJobRepository.saveAll(searchJobEntities);
            }
        }

        return RepeatStatus.FINISHED;
    }

    /**
     * 검색 정보 엔티티 추출
     * 
     * @param  searchInfoEntityByTsiUno (검색 정보 엔티티)
     * @param  searchResultEntity       (검색 결과 엔티티)
     * @return SearchInfoEntity         (검색 정보 엔티티)
     */
    private SearchInfoEntity getSearchInfoEntity(SearchInfoEntity searchInfoEntityByTsiUno, SearchResultEntity searchResultEntity) {
        SearchInfoEntity searchInfoEntity = new SearchInfoEntity();

        searchInfoEntity.setUserUno(1);
        searchInfoEntity.setTsiStat("11");

        //기존 searchInfo 값 세팅 (기본 정보)
        //배치는 11, 17 제외 13
        //11:키워드 이거나 17:이미지 이면 그대로
        if ("11".equals(searchInfoEntityByTsiUno.getTsiType()) || "17".equals(searchInfoEntityByTsiUno.getTsiType())) {
            searchInfoEntity.setTsiType(searchInfoEntityByTsiUno.getTsiType());         //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
        }
        //나머지는 13:키워드+이미지
        else {
            searchInfoEntity.setTsiType("13");                                          //검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
        }
        searchInfoEntity.setTsiGoogle(searchInfoEntityByTsiUno.getTsiGoogle());         //구글 검색 여부
        searchInfoEntity.setTsiTwitter(searchInfoEntityByTsiUno.getTsiTwitter());       //트위터 검색 여부
        searchInfoEntity.setTsiFacebook(searchInfoEntityByTsiUno.getTsiFacebook());     //페이스북 검색 여부
        searchInfoEntity.setTsiInstagram(searchInfoEntityByTsiUno.getTsiInstagram());   //인스타그램 검색 여부
        searchInfoEntity.setTsiKeyword(searchInfoEntityByTsiUno.getTsiKeyword());       //검색어

        //기존 searchResult 이미지 파일 복사
        String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filePath = fileLocation1 + folder;
        String uuid = UUID.randomUUID().toString();
        String extension = searchResultEntity.getTsrImgExt();
        File destdir = new File(filePath);
        if (!destdir.exists()) {
            destdir.mkdirs();
        }

        File srcFile  = new File(searchResultEntity.getTsrImgPath() + searchResultEntity.getTsrImgName());
        File destFile = new File(destdir + File.separator + uuid + "." + extension);

        try {
            FileUtils.copyFile(srcFile, destFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        //기존 searchResult 값 세팅 (이미지 정보)
        searchInfoEntity.setTsiImgPath((destdir + File.separator).replaceAll("\\\\", "/"));
        searchInfoEntity.setTsiImgName(uuid + "." + extension);
        searchInfoEntity.setTsiImgExt(searchResultEntity.getTsrImgExt());
        searchInfoEntity.setTsiImgHeight(searchResultEntity.getTsrImgHeight());
        searchInfoEntity.setTsiImgWidth(searchResultEntity.getTsrImgWidth());
        searchInfoEntity.setTsiImgSize(searchResultEntity.getTsrImgSize());
        searchInfoEntity.setTsrUno(searchResultEntity.getTsrUno());

        //검색 정보 엔티티 기본값 세팅
        searchService.setSearchInfoDefault(searchInfoEntity);

        return searchInfoEntity;
    }


    /**
     * 모든 결과 List 추출
     *
     * @param  searchInfoEntity (검색 이력 엔티티)
     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
     * @param  getErrorFn       (info error getter Function)
     * @param  getSubFn         (RESULT getter Function)
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    private <INFO, RESULT> List<RESULT> getAllResults(SearchInfoEntity searchInfoEntity
            , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram) {
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
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
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
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
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
                            , getLinkFn
                            , isFacebookFn
                            , isInstagram
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
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
            , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram) {
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
                , getLinkFn
                , isFacebookFn
                , isInstagram
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
     * @param  getLinkFn        (link getter Function)
     * @param  isFacebookFn     (isFacebook Function)
     * @param  isInstagram      (isInstagram Function)
     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
            , List<RESULT> results, int index, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn
            , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram) {
        boolean isFirst = index == 0;
        boolean loop = true;

        List<CompletableFuture<List<RESULT>>> completableFutures = new ArrayList<>();

        //기존 SearchService 에 있던 부분 활용
        do {
            String url = getUrl(tsiKeyword, index, infoClass, searchInfoEntity);
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

        List<RESULT> searchResults;
        //결과 값을 받아온다.
        try {
            searchResults = completableFutures.stream().map(CompletableFuture::join).flatMap(s -> s.stream()).toList();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

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
                    , getLinkFn
                    , isFacebookFn
                    , isInstagram
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
     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
     * @param  searchInfoEntity (검색 정보 엔티티)
     * @return String           (URL)
     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
     */
    private <INFO> String getUrl(String tsiKeyword, int index, Class<INFO> infoClass, SearchInfoEntity searchInfoEntity) {
        String url = null;

        //텍스트 검색
        if (YandexByTextResult.class == infoClass) {
            // yandex search url
            url = textYandexUrl
                    + "?q=" + tsiKeyword
                    + "&GL=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&tbm=" + textYandexTbm
                    + "&ijn=" + index
                    + "&api_key=" + textYandexApikey
                    + "&engine=" + textYandexEngine;
        }
        //이미지 검색
        else if (YandexByImageResult.class == infoClass) {
            String searchImageUrl = searchInfoEntity.getTsiImgPath() + searchInfoEntity.getTsiImgName();
            searchImageUrl = serverIp + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);

            url = textYandexUrl
                    + "?GL=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;
        }

        return url;
    }

    /**
     * 결과를 검색 결과 엔티티로 변환
     *
     * @param  tsiUno                   (검색 정보 테이블의 key)
     * @param  results                  (결과 List)
     * @param  getOriginalFn            (original getter Function)
     * @param  getThumbnailFn           (thumbnail getter Function)
     * @param  getTitleFn               (title getter Function)
     * @param  getLinkFn                (link getter Function)
     * @param  isFacebookFn             (isFacebook Function)
     * @param  isInstagramFn            (isInstagram Function)
     * @return List<SearchResultEntity> (검색 결과 엔티티 List)
     * @param  <RESULT>                 (결과)
     */
    private <RESULT> List<SearchResultEntity> resultsToSearchResultEntity(int tsiUno, List<RESULT> results
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) {
        log.info(" ### resultsToSearchResultEntity ### ");

        return results.stream().map(result -> {
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
            SearchResultEntity searchResultEntity = searchService.getSearchResultEntity(tsiUno, tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

            try {
                //이미지 파일 저장
                searchService.saveImageFile(tsiUno, restTemplate, searchResultEntity, result, getOriginalFn, getThumbnailFn);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }

            //검색 결과 엔티티 기본값 세팅
            searchService.setSearchResultDefault(searchResultEntity);

            return searchResultEntity;
        }).toList();
    }

    /**
     * 검색 결과 엔티티를 검색 작업 엔티티로 변환
     * 
     * @param  searchResultEntities  (검색 결과 엔티티 List)
     * @return List<SearchJobEntity> (검색 작업 엔티티 List)
     */
    private List<SearchJobEntity> searchResultEntityToSearchJobEntity(List<SearchResultEntity> searchResultEntities) {
        return searchResultEntities.stream().map(searchResultEntity -> {
            //검색 작업 엔티티 추출
            SearchJobEntity searchJobEntity = searchService.getSearchJobEntity(searchResultEntity);

            //검색 작업 엔티티 기본값 세팅
            searchService.setSearchJobDefault(searchJobEntity);

            return searchJobEntity;
        }).toList();
    }

}