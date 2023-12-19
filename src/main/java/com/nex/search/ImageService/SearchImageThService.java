package com.nex.search.ImageService;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.search.entity.*;
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
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchImageThService {
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
    @Value("${search.server.url}")
    private String serverIp2;

    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public void search(byte tsiGoogle, byte tsiFacebook, byte tsiInstagram, byte tsiTwitter, String tsiType, SearchInfoEntity insertResult, String folder,
                           SearchInfoDto searchInfoDto){
        String tsrSns = "11";
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp2 + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);
        searchSnsByImage(searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchSnsByImage(String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        int index=0;

        String textYandexGl = "th";
        String finalTextYandexGl1 = textYandexGl;

        searchByImage(index, finalTextYandexGl1, searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchByImage(int index, String finalTextYandexGl1, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchYandex(index, finalTextYandexGl1,searchImageUrl,searchInfoDto, tsrSns, YandexByImageResult.class, YandexByImageResult::getError, YandexByImageResult::getInline_images);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImage::getOriginal
                                , Images_resultsByImage::getThumbnail
                                , Images_resultsByImage::getTitle
                                , Images_resultsByImage::getSource
                                , Images_resultsByImage::isFacebook
                                , Images_resultsByImage::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try {// db에 적재.
                        return saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenRun(()->{
                    if(loop == true){
                        log.info("loop == true 진입: " + loop);
                        CompletableFutureYandexByImage(index, finalTextYandexGl1,searchImageUrl,searchInfoDto, tsrSns,insertResult);
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchYandex(int index,String finalTextYandexGl1, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        String url = textYandexUrl
                + "?gl=" + finalTextYandexGl1
                + "&no_cache=" + textYandexNocache
                + "&api_key=" + textYandexApikey
                + "&safe=off"
                + "&filter=0"
                + "&nfpr=0"
                + "&start=" + String.valueOf(index * 10)
                // + "&tbm=" + textYandexTbm
                + "&engine=" + imageYandexEngine
                + "&image_url=" + searchImageUrl;

        log.info("searchYandex 진입");
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        List<RESULT> results = null;

        log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "images_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
        }

        if(results == null || index >= Integer.parseInt(textYandexCountLimit) - 1) {
            loop=false;
        }

        // if(index >1){ loop=false;}

        log.info("results: " + results);
        log.debug("searchYandex loop: " + loop);

        return results != null ? results : new ArrayList<>();
    }

    public <RESULT> List<SearchResultEntity> saveYandex(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws Exception {
        log.info("========= saveYandex 진입 =========");

        if (results == null) {
            log.info("result null");
            return null;
        }

        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();

        //SearchResultEntity sre = null;
        for (RESULT result : results) {
            log.info("results: " + results);

            try {
                String imageUrl = getOriginalFn.apply(result) ;
                log.info("imageUrl1: "+imageUrl);
                if(imageUrl == null) {
                    imageUrl = getThumbnailFn.apply(result);
                }
                log.info("imageUrl2: "+imageUrl);
                if(imageUrl != null) {
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = searchService.getSearchResultGoogleReverseEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

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
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
        SearchInfoEntity updateResult = searchService.saveSearchInfo_2(insertResult);

        List<SearchResultEntity> searchResultEntity = result;


        //SearchJobEntity sje = null;
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

    public void CompletableFutureYandexByImage(int index, String finalTextYandexGl1, String searchImageUrl,SearchInfoDto searchInfoDto,  String tsrSns, SearchInfoEntity insertResult) {
        index++;
        int finalIndex = index;

        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchYandex(finalIndex, finalTextYandexGl1, searchImageUrl, searchInfoDto,tsrSns, YandexByImageResult.class, YandexByImageResult::getError, YandexByImageResult::getInline_images);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImage::getOriginal
                                , Images_resultsByImage::getThumbnail
                                , Images_resultsByImage::getTitle
                                , Images_resultsByImage::getSource
                                , Images_resultsByImage::isFacebook
                                , Images_resultsByImage::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        return saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenRun(()->{
                    if(loop == true){
                        CompletableFutureYandexByImage(finalIndex, finalTextYandexGl1,searchImageUrl,searchInfoDto, tsrSns,insertResult);
                    }
                });
    }


}
