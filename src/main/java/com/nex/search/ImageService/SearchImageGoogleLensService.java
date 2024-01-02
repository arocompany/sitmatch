package com.nex.search.ImageService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
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

    public void searchYandexByGoogleLensImage(String tsrSns, SearchInfoEntity insertResult) throws JsonProcessingException {
        log.info("searchYandexByGoogleLensImage 진입");
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp2 + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);

        // test용 이미지
        // searchImageUrl = "http://106.254.235.202:9091/imagePath/requests/20231129/f7497f8d-4784-4841-8897-e213a878cca3.jpg"; // 조세호
        // searchImageUrl = "http://106.254.235.202:9091/imagePath/requests/20231021/563ca536-01dd-4931-a7f8-9b93cc1dbd54.jpg"; // 아청물

        log.info("searchImageUrl: "+searchImageUrl);
        // CompletableFutureGoogleLensByImage(searchImageUrl, tsrSns, insertResult);

        try {
            log.info("== cn 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=cn"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByCnImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }


        try {
            log.info("== kr 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=kr"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByKrImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            log.info("== nl 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=nl"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByNlImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            log.info("== ru 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=ru"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByRuImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            log.info("== th 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=th"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByThImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            log.info("== us 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=us"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByUsImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            log.info("== vn 시작 ==");
            String url = textYandexUrl
                    + "?engine=google_lens"
                    + "&url=" + searchImageUrl
                    + "&country=vn"
                    + "&api_key="+textYandexApikey;

            log.info("url: " + url);

            CompletableFutureGoogleLensByVnImage(url, tsrSns, insertResult);

        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void CompletableFutureGoogleLensByCnImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchCnGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByKrImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchKrGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByNlImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchNlGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByRuImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchRuGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByThImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchThGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByUsImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchUsGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }


    public void CompletableFutureGoogleLensByVnImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchVnGoogleLens(url, GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources);
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
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }
    
    // searchCnGoogleLens 시작
    public <INFO, RESULT> List<RESULT> searchCnGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=cn"
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

    public <INFO, RESULT> List<RESULT> searchKrGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=kr"
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

    public <INFO, RESULT> List<RESULT> searchNlGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("searchYandex 진입: "+url);

        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String jsonInString = mapper.writeValueAsString(resultMap.getBody());
        JsonNode rootNode = mapper.readTree(jsonInString);
        String pageToken = rootNode.at("/image_sources_search/page_token").asText();

        // pageToken값 출력
        System.out.println("page_token: " + pageToken);

        String url2 = textYandexUrl
                + "?engine=google_lens_image_sources"
                + "&page_token=" + pageToken
                + "&country=nl"
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

    public <INFO, RESULT> List<RESULT> searchRuGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=ru"
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

    public <INFO, RESULT> List<RESULT> searchThGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=th"
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

    public <INFO, RESULT> List<RESULT> searchUsGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=us"
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

    public <INFO, RESULT> List<RESULT> searchVnGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
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
                + "&country=vn"
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

}
