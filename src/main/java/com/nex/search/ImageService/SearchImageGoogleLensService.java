package com.nex.search.ImageService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.result.GoogleLensImagesByImageResult;
import com.nex.search.entity.result.Images_resultsByGoogleLens;
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
public class SearchImageGoogleLensService {
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
    private final RestTemplate restTemplate;
    private String nationCode = "";

    public void searchYandexByGoogleLensImage(String tsrSns, SearchInfoEntity insertResult, String nationCode) throws JsonProcessingException {
        log.info("searchYandexByGoogleLensImage 진입");
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp2 + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);

        // test용 이미지
        // searchImageUrl = "http://106.254.235.202:9091/imagePath/requests/20240102/e89c63da-d7ed-48b6-a9a3-056fe582b6b2.jpg"; //고양이

        this.nationCode = nationCode;
        String finalTextYandexGl1=this.nationCode;

        log.info("searchImageUrl: "+searchImageUrl);
        // CompletableFutureGoogleLensByImage(searchImageUrl, tsrSns, insertResult);
        try {
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country="+finalTextYandexGl1
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByImage(url, tsrSns, insertResult, finalTextYandexGl1);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void CompletableFutureGoogleLensByImage(String url, String tsrSns, SearchInfoEntity insertResult, String finalTextYandexGl1) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources, finalTextYandexGl1);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try { // 결과 저장.(이미지)
                        return saveGoogleLens(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByGoogleLens::getTitle
                                , Images_resultsByGoogleLens::getLink
                                , Images_resultsByGoogleLens::getThumbnail
                                , Images_resultsByGoogleLens::isFacebook
                                , Images_resultsByGoogleLens::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try { // 결과 db에 적재.
                        if(r==null){ return null; }
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, String finalTextYandexGl1) throws Exception {
        log.info("searchYandex 진입: "+url);

        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String jsonInString = mapper.writeValueAsString(resultMap.getBody());
        JsonNode rootNode = mapper.readTree(jsonInString);
        String pageToken = rootNode.at("/image_sources_search/page_token").asText();

        System.out.println("page_token: " + pageToken);

        // pageToken값 출력
        System.out.println("page_token: " + pageToken);

        String url2 = textYandexUrl
                + "?engine=google_lens_image_sources"
                + "&page_token=" + pageToken
                + "&country="+finalTextYandexGl1
                + "&safe=off"
                + "&api_key="+textYandexApikey;

        HttpHeaders header2 = new HttpHeaders();
        HttpEntity<?> entity2 = new HttpEntity<>(header2);
        UriComponents uri2 = UriComponentsBuilder.fromHttpUrl(url2).build();
        ResponseEntity<?> resultMap2 = new RestTemplate().exchange(uri2.toString(), HttpMethod.GET, entity2, Object.class);

        List<RESULT> results = null;

        log.debug("resultMap.getStatusCodeValue(): " + resultMap2.getStatusCodeValue());

        if (resultMap2.getStatusCodeValue() == 200) {
            ObjectMapper mapper2 = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString2 = mapper2.writeValueAsString(resultMap2.getBody());
            INFO info = mapper2.readValue(jsonInString2, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
        }

        log.debug("results: " + results);
        return results != null ? results : new ArrayList<>();
    }

    public <RESULT> List<SearchResultEntity> saveGoogleLens(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws Exception {
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
                String imageUrl = getThumbnailFn.apply(result);
                log.info("imageUrl1: "+imageUrl);
                if(imageUrl != null) {
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = searchService.getSearchResultGoogleLensEntity(insertResult.getTsiUno(), tsrSns, result, getThumbnailFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

                    //이미지 파일 저장
                    searchService.saveGoogleLensImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getThumbnailFn, getThumbnailFn);
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


    public String saveImgSearchGoogleLens(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
        // SearchInfoEntity updateResult = saveSearchInfo_2(insertResult);
        searchService.saveSearchInfo_2(insertResult);
        List<SearchResultEntity> searchResultEntity = result;

        if(searchResultEntity != null) {
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
        }
        return "저장 완료";
    }

}
