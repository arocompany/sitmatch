package com.nex.search.textGoogleService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByText;
import com.nex.search.entity.result.YandexByTextResult;
import com.nex.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchTextKrGoogleService {
    private final SearchService searchService;

    @Autowired
    ResourceLoader resourceLoader;

    @Value("${file.location2}")
    private String fileLocation2;
    @Value("${python.video.module}")
    private String pythonVideoModule;
    @Value("${search.yandex.text.url}")
    private String textYandexUrl;

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
    private String textYandexCountLimit;
    @Value("${file.location1}")
    private String fileLocation1;
    @Value("${file.location3}")
    private String fileLocation3;
    @Value("${server.url}")
    private String serverIp;

    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){
        String tsrSns = "11";
        // searchText(tsiType, insertResult, folder, tsrSns, searchInfoDto);
        searchSnsByText(tsrSns, insertResult, searchInfoDto);
    }

    public void searchSnsByText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
        int index=0;

        String textYandexGl = "kr";
        String finalTextYandexGl1 = textYandexGl;

        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();
        searchByText(index, finalTextYandexGl1, tsrSns, insertResult, searchInfoDto);
    }

    public void searchByText(int index, String finalTextYandexGl1, String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        // return searchTextYandex(index, searchInfoDto, tsrSns, finalTextYandexGl1, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                        return searchTextYandex(index, searchInfoDto, tsrSns, finalTextYandexGl1, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        log.info("R" + r);

                        // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByText::getOriginal
                                , Images_resultsByText::getThumbnail
                                , Images_resultsByText::getTitle
                                , Images_resultsByText::getLink
                                , Images_resultsByText::isFacebook
                                , Images_resultsByText::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenAccept((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }).thenRun(()-> {
                    try {
                        CompletableFutureYandexByText(index, tsrSns, finalTextYandexGl1, insertResult,searchInfoDto);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

    }

    public <INFO, RESULT> List<RESULT> searchTextYandex(int index, SearchInfoDto searchInfoDto, String tsrSns, String textYandexGl, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("============== searchTextYandex index: "+index+ " textYandexGl"+textYandexGl);
        String tsiKeywordHiddenValue2 = searchInfoDto.getTsiKeywordHiddenValue();

        //인스타
        if ("15".equals(tsrSns)) {
            tsiKeywordHiddenValue2 = "인스타그램 " + tsiKeywordHiddenValue2;
        }
        //페북
        else if ("17".equals(tsrSns)) {
            tsiKeywordHiddenValue2 = "페이스북 " + tsiKeywordHiddenValue2;
        }

        String url = textYandexUrl
                + "?q=" + tsiKeywordHiddenValue2
                + "&gl=" + textYandexGl
                + "&no_cache=" + textYandexNocache
                + "&location=" + textYandexLocation
                + "&start=" + String.valueOf(index * 10)
                + "&api_key=" + textYandexApikey
                + "&safe=off"
                + "&filter=0"
                + "&nfpr=0"
                + "&engine=google";

        log.info("tsiKeywordHiddenValue2" +tsiKeywordHiddenValue2);
        log.info("searchTextYandex url: " +url);

        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        // ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<RESULT> results = null;

        log.info("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());
        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "images_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
            /*
            log.info("mapper: "+ mapper);
            log.info("jsonInString: "+ jsonInString);
            log.info("info: "+ info);
            */
            log.info("searchTextYandex results: "+results);

        }

        if(results == null || index >= Integer.parseInt(textYandexCountLimit) - 1) {
            loop=false;
        }

        // if(index >1){ loop=false;}

        return results != null ? results : new ArrayList<>();
    }

    public <RESULT> List<SearchResultEntity> saveYandex(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws Exception {
        log.info("=========== saveYandex 진입 ==============");
        if (results == null) {
            loop=false;
            return null;
        }

        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();

        for (RESULT result : results) {

            try {
                String imageUrl = getOriginalFn.apply(result);
                log.info("imageUrl1: "+imageUrl);
                if(imageUrl == null) {
                    imageUrl = getThumbnailFn.apply(result);
                }
                log.info("imageUrl2: "+imageUrl);

                if(imageUrl != null) {
                    SearchResultEntity sre = searchService.getSearchResultTextEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    //이미지 파일 저장
                    searchService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn);
                    searchService.saveSearchResult(sre);

                    sreList.add(sre);
                }

            } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }

    public String saveImgSearchYandex(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        log.info("==========saveImgSearchYandex 진입==========");
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }

        SearchInfoEntity updateResult = searchService.saveSearchInfo_2(insertResult);
        List<SearchResultEntity> searchResultEntity = result;

        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = searchService.getSearchJobEntity(sre);
                searchService.saveSearchJob(sje);
            } catch (JpaSystemException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                throw new JpaSystemException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }

        return "저장 완료";
    }

    public void CompletableFutureYandexByText(int index, String tsrSns, String textYandexGl, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) throws ExecutionException, InterruptedException {
        log.info("--------------- CompletableFutureYandexByText index 값: " + index+ " textYandexGl: " + textYandexGl);

        index++;
        int finalIndex = index;

        CompletableFuture.supplyAsync(() -> {
            try {
                // text기반 yandex 검색
                return searchTextYandex(finalIndex, searchInfoDto, tsrSns,textYandexGl, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }).thenApply((r) -> {
            try {
                if (r == null) {
                    log.info("텍스트검색 r == null 진입");
                    loop = false;
                }
                log.info(" --------------- loop 값 --------------- " + loop + " textYandexGl "+textYandexGl);
                // 결과 저장.(이미지)
                return saveYandex(
                        r
                        , tsrSns
                        , insertResult
                        , Images_resultsByText::getOriginal
                        , Images_resultsByText::getThumbnail
                        , Images_resultsByText::getTitle
                        , Images_resultsByText::getLink
                        , Images_resultsByText::isFacebook
                        , Images_resultsByText::isInstagram
                );
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }).thenAccept((r) -> {
            try {
                log.info("==== thenAccept 진입 ===="+" textYandexGl "+textYandexGl);
                if (r != null) {
                    // yandex검색을 통해 결과 db에 적재.
                    saveImgSearchYandex(r, insertResult);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).thenRun(()->{
            log.info("thenRun loop값: "+loop);
            if(loop == true){
                try {
                    log.info("loop==true 진입:" + loop);
                    log.info("==== thenRun 진입 ==== index값: " + finalIndex+" textYandexGl "+textYandexGl);
                    CompletableFutureYandexByText(finalIndex, tsrSns, textYandexGl, insertResult,searchInfoDto);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return;
            }

        });

        // results.get();

    }

}
