package com.nex.batch.tracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.repository.NationCodeRepository;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.RequestSerpApiLogEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.service.*;
import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.repo.SerpServicesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingSearchResultService{

    private final SearchService searchService;
    private final ImageService imageService;

    private final SitProperties sitProperties;
    private final RestTemplate restTemplate;
    private final NationCodeRepository nationCodeRepository;
    private final SerpServicesRepository serpServicesRepository;

    private final RequestSerpApiLogService requestSerpApiLogService;

    private final RestTemplateConfig restTemplateConfig;

    private final SearchTextService searchTextService;
    private final SearchYoutubeService searchYoutubeService;
    private final SearchTextBaiduService searchTextBaiduService;
    private final SearchTextBingService searchTextBingService;
    private final SearchTextDuckduckgoService searchTextDuckduckgoService;
    private final SearchTextYahooService searchTextYahooService;
    private final SearchTextYandexService searchTextYandexService;
    private final SearchTextNaverService searchTextNaverService;

//    /**
//     * 모든 결과 List 추출
//     *
//     * @param  searchInfoEntity (검색 이력 엔티티)
//     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
//     * @param  getErrorFn       (info error getter Function)
//     * @param  getSubFn         (RESULT getter Function)
//     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
//     * @param  getLinkFn        (link getter Function)
//     * @param  isFacebookFn     (isFacebook Function)
//     * @param  isInstagram      (isInstagram Function)
//     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
//     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
//     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
//     */
    public  <INFO, RESULT> List<RESULT> getAllResults(SearchInfoEntity searchInfoEntity
           , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
           , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Function<RESULT, Boolean> isTwitter, Boolean isText
    ) throws JsonProcessingException {
        log.info("모든 결과 List 추출 getAllResults 진입");
        List<String> siteUrls = searchService.findTsrSiteUrlDistinctByTsiUno(searchInfoEntity.getTsiUno());

        String tsiKeyword = searchInfoEntity.getTsiKeyword();

        //모든 결과 List
        List<RESULT> allResults = new ArrayList<>();

        //구글 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiGoogle())) {
            allResults.addAll(getResults(tsiKeyword, searchInfoEntity, Consts.GOOGLE, siteUrls, infoClass, getErrorFn, getSubFn, setTsiUnoCn, getLinkFn, isFacebookFn, isInstagram, isTwitter, isText));
        }
        //페이스북 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiFacebook())) {
            allResults.addAll(getResults("페이스북 " + tsiKeyword, searchInfoEntity, Consts.FACEBOOK, siteUrls, infoClass, getErrorFn, getSubFn, setTsiUnoCn, getLinkFn, isFacebookFn, isInstagram, isTwitter, isText));
        }
        //인스타그램 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiInstagram())) {
            allResults.addAll(getResults("인스타그램 " + tsiKeyword, searchInfoEntity, Consts.INSTAGRAM, siteUrls, infoClass, getErrorFn, getSubFn, setTsiUnoCn, getLinkFn, isFacebookFn, isInstagram, isTwitter, isText));
        }

        //인스타그램 검색
        if (BooleanUtils.toBoolean(searchInfoEntity.getTsiTwitter())) {
            allResults.addAll(getResults("트위터 " + tsiKeyword, searchInfoEntity, Consts.TWITTER, siteUrls, infoClass, getErrorFn, getSubFn, setTsiUnoCn, getLinkFn, isFacebookFn, isInstagram, isTwitter, isText));
        }

        return allResults;
    }

    //    /**
//     * 결과 List 추출
//     *
//     * @param  tsiKeyword       (검색어)
//     * @param  searchInfoEntity (검색 정보 엔티티)
//     * @param  dvn              (구분)
//     * @param  siteUrls         (사이트 URL List)
//     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
//     * @param  getErrorFn       (info error getter Function)
//     * @param  getSubFn         (RESULT getter Function)
//     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
//     * @param  getLinkFn        (link getter Function)
//     * @param  isFacebookFn     (isFacebook Function)
//     * @param  isInstagram      (isInstagram Function)
//     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
//     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
//     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
//     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
           , Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
           , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Function<RESULT, Boolean> isTwitter, Boolean isText) throws JsonProcessingException {
        log.info("결과 List 추출 getResults 진입");
        List<RESULT> results = new ArrayList<>();

        return getResults(tsiKeyword, searchInfoEntity, dvn, siteUrls, results, 0, infoClass, getErrorFn, getSubFn, setTsiUnoCn, getLinkFn, isFacebookFn, isInstagram, isTwitter, isText);
    }


//    /**
//     * 이미지 결과 목록 추출
//     *
//     * @param  tsiKeyword       (검색어)
//     * @param  searchInfoEntity (검색 정보 엔티티)
//     * @param  dvn              (구분)
//     * @param  siteUrls         (사이트 URL List)
//     * @param  results          (결과 List)
//     * @param  index            (인덱스)
//     * @param  infoClass        (YandexByTextResult or YandexByImageResult Class)
//     * @param  getErrorFn       (info error getter Function)
//     * @param  getSubFn         (RESULT getter Function)
//     * @param  setTsiUnoCn      (tsiUno setter BiConsumer)
//     * @param  getLinkFn        (link getter Function)
//     * @param  isFacebookFn     (isFacebook Function)
//     * @param  isInstagram      (isInstagram Function)
//     * @return List<RESULT>     (Images_resultsByText or Images_resultsByImage List)
//     * @param  <INFO>           (YandexByTextResult or YandexByImageResult)
//     * @param  <RESULT>         (Images_resultsByText or Images_resultsByImage)
//     */
    private <INFO, RESULT> List<RESULT> getResults(String tsiKeyword, SearchInfoEntity searchInfoEntity, String dvn, List<String> siteUrls
           , List<RESULT> results, int index, Class<INFO> infoClass, Function<INFO, String> getErrorFn
           , Function<INFO, List<RESULT>> getSubFn, BiConsumer<RESULT, Integer> setTsiUnoCn
           , Function<RESULT, String> getLinkFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagram, Function<RESULT, Boolean> isTwitter, boolean isText) throws JsonProcessingException {
        log.info("이미지 결과 목록 추출 getResults 진입, index : "+index);

        List<CompletableFuture<List<RESULT>>> completableFutures = new ArrayList<>();

        List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
        List<SerpServicesEntity> ssList = serpServicesRepository.findBySsIsActive(1);

        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        int cntNation = 0;
        for(NationCodeEntity ncInfo : ncList){
            for(SerpServicesEntity ssInfo : ssList) {
                String url = "";
                int pageNo = 0;

                url = getUrl(ssInfo, searchInfoEntity, tsiKeyword, ncInfo, pageNo, cntNation, isText, dvn);

                String searchImageUrl = searchInfoEntity.getTsiImgPath() + searchInfoEntity.getTsiImgName();
                searchImageUrl = configData.getHostImageUrl() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);

                if(!StringUtils.hasText(url)){
                    continue;
                }

                RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(searchInfoEntity.getTsiUno(), url, ncInfo.getNcCode().toLowerCase(), ssInfo.getSsName(), tsiKeyword, index, configData.getSerpApiKey(), searchImageUrl);

                String realUrl = url;

                //기존 SearchService 에 있던 부분 활용
                CompletableFuture<List<RESULT>> listCompletableFuture = CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                List<RESULT> list = searchBatch(realUrl, infoClass, getErrorFn, getSubFn, rsalEntity);
                                return list;
                            } catch (Exception e) {
                                log.debug(e.getMessage());
                            }
                            return null;
                        });

                completableFutures.add(listCompletableFuture);

                int numberOfApiCalls = sitProperties.getTextCountLimit();
                for (int i = 1; i < numberOfApiCalls; i++) {
                    int currentApiNumber = i;

                    String searchImageUrl2 = searchImageUrl;
                    listCompletableFuture = listCompletableFuture.thenComposeAsync(previousResult -> {
                        // 이전 API의 결과를 확인하고 다음 외부 API 호출

                        String finalUrl = getUrl(ssInfo, searchInfoEntity, tsiKeyword, ncInfo, currentApiNumber, cntNation, isText, dvn);
                        if (previousResult != null && !previousResult.isEmpty() && previousResult.get(0) != null && StringUtils.hasText(finalUrl)) {

                            RequestSerpApiLogEntity rsalEntitySecond = requestSerpApiLogService.init(searchInfoEntity.getTsiUno(), finalUrl, ncInfo.getNcCode().toLowerCase(), ssInfo.getSsName(), tsiKeyword, currentApiNumber, configData.getSerpApiKey(), searchImageUrl2);
//                            RequestSerpApiLogEntity rsalEntitySecond2 = rsalEntitySecond;
                            return CompletableFuture.supplyAsync(() -> {
                                try {
                                    List<RESULT> list = searchBatch(finalUrl, infoClass, getErrorFn, getSubFn, rsalEntitySecond);
                                    return list;
                                } catch (Exception e) {
                                    log.debug(e.getMessage());
                                }
                                return null;
                            });
                        } else {
                            // 이전 API의 결과가 없으면 빈 CompletableFuture 반환
                            return CompletableFuture.completedFuture(Collections.emptyList());
                        }
                    });

                    completableFutures.add(listCompletableFuture);
                }
            }
        }

        //결과 값을 받아온다.
        List<RESULT> searchResults = completableFutures.stream().map(CompletableFuture::join).filter(Objects::nonNull).flatMap(s -> s.stream()).toList();

        int size = searchResults.size();
        log.info("size: " + size);

        //페이스북으로 필터
        if (Consts.FACEBOOK.equals(dvn)) { searchResults = searchResults.stream().filter(searchResult -> isFacebookFn.apply(searchResult)).toList();}
        //인스타그램으로 필터
        if (Consts.INSTAGRAM.equals(dvn)) { searchResults = searchResults.stream().filter(searchResult -> isInstagram.apply(searchResult)).toList(); }
        //트위터으로 필터
        if (Consts.TWITTER.equals(dvn)) { searchResults = searchResults.stream().filter(searchResult -> isTwitter.apply(searchResult)).toList(); }

        //DB에 저장 되어 있지 않은 url 필터
        searchResults = searchResults.stream().filter(searchResult -> !siteUrls.contains(getLinkFn.apply(searchResult))).toList();

        searchResults.forEach(searchResult -> setTsiUnoCn.accept(searchResult, searchInfoEntity.getTsiUno()));

        //return 값에 담는다.
        results.addAll(searchResults);
        return results;
    }
//
//    /**
//     * URL 추출
//     *
//     * @param  tsiKeyword       (검색어)
//     * @param  index            (인덱스)
//     * @param  isText           (텍스트 검색 여부)
//     * @param  searchInfoEntity (검색 정보 엔티티)
//     * @return String           (URL)
//     */
//    private String getUrl(String tsiKeyword, int index, Boolean isText, SearchInfoEntity searchInfoEntity, String lang) {
//        log.info("getUrl 진입");
//        String url;
//
//        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
//
//        //텍스트 검색
//        if (isText) {
//            log.info("텍스트검색 getUrl 진입");
//            // yandex search url
//             url = sitProperties.getTextUrl()
//                    + "?q=" + tsiKeyword
//                    + "&gl=" + lang
//                    + "&no_cache=" + sitProperties.getTextNocache()
//                    + "&location=" + sitProperties.getTextLocation()
//                    + "&tbm=" + sitProperties.getTextTbm()
//                    + "&start=" + String.valueOf(index*10)
//                    + "&safe=off"
//                    + "&filter=0"
//                    + "&nfpr=0"
//                    + "&api_key=" + configData.getSerpApiKey()
//                    + "&engine=google";
//
//        }
//        //이미지 검색
//        else {
//            log.info("이미지검색 getUrl 진입");
//            String searchImageUrl = searchInfoEntity.getTsiImgPath() + searchInfoEntity.getTsiImgName();
//            searchImageUrl = configData.getHostImageUrl() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);
//            // searchImageUrl = searchImageUrl.replace("172.20.7.100","222.239.171.250");
//            // searchImageUrl = "http://106.254.235.202:9091/imagePath/requests/20240102/e89c63da-d7ed-48b6-a9a3-056fe582b6b2.jpg"; //고양이
//
//            url = sitProperties.getTextUrl()
//                    + "?gl=" + lang
//                    + "&no_cache=" + sitProperties.getTextNocache()
//                    + "&api_key=" + configData.getSerpApiKey()
//                    + "&safe=off"
//                    + "&filter=0"
//                    + "&nfpr=0"
//                    + "&start=" + String.valueOf(index*10)
//                    + "&engine=" + sitProperties.getImageEngine()
//                    + "&image_url=" + searchImageUrl;
//        }
//
//        return url;
//    }


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
           , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        List<CompletableFuture<SearchResultEntity>> completableFutures = new ArrayList<>();

        log.info(" searchResult processor");
        log.info("results count === {}", results.size());

        for (RESULT result : results) {
            CompletableFuture<SearchResultEntity> completableFuture = CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            String tsrSns; //SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북)
                            // RestTemplate restTemplate = new RestTemplate();     //RestTemplate
                            if (isFacebookFn.apply(result)) { tsrSns = CommonCode.snsTypeFacebook; }
                            else if (isInstagramFn.apply(result)) { tsrSns = CommonCode.snsTypeInstagram; }
                            else if (isTwitterFn.apply(result)) { tsrSns = CommonCode.snsTypeTwitter; }
                            else { tsrSns = CommonCode.snsTypeGoogle; }

                            //검색 결과 엔티티 추출
                            SearchResultEntity searchResultEntity = CommonStaticSearchUtil.getSearchResultEntity2(getTsiUnoFn.apply(result), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                            try {
                                //이미지 파일 저장
                                imageService.saveImageFile(getTsiUnoFn.apply(result), restTemplate, searchResultEntity, result, getOriginalFn, getThumbnailFn, false);
                            } catch (IOException e) {
                                log.error(e.getMessage(), e);
                                e.printStackTrace();
//                                throw new RuntimeException(e);
                            }

                            //검색 결과 엔티티 기본값 세팅
                            CommonStaticSearchUtil.setSearchResultDefault(searchResultEntity);

                            return searchResultEntity;
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                        }
                        return null;
                    });

            completableFutures.add(completableFuture);
        }

        //결과 값을 받아온다.
        List<SearchResultEntity> searchResults = completableFutures.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();

        return searchResults;
    }

        // 배치시 진입
    public <INFO, RESULT> List<RESULT> searchBatch(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, RequestSerpApiLogEntity rsalEntity) throws Exception {
        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            log.info("searchBatch 진입 url === {}", url);
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            // ResponseEntity<?> resultMap = new customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
            ResponseEntity<?> resultMap = restTemplateConfig.customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

            List<RESULT> results = null;

//            log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

            if(rsalEntity == null) {
                log.error("rsalEntity is null");
            }else{
                requestSerpApiLogService.save(rsalEntity);
            }


            if(rsalEntity != null && rsalEntity.getRslEngine().equals("google_lens")) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody());
                JsonNode rootNode = mapper.readTree(jsonInString);
                String pageToken = rootNode.at("/image_sources_search/page_token").asText();

                String sourcesUrl = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), null, rsalEntity.getRslNation(), null, null, null, configData.getSerpApiKey(), null, "google_lens_image_sources", pageToken);

                HttpHeaders sourcesHeader = new HttpHeaders();
                HttpEntity<?> sourcesEntity = new HttpEntity<>(sourcesHeader);
                UriComponents sourcesUri = UriComponentsBuilder.fromHttpUrl(sourcesUrl).build();
                ResponseEntity<?> sourcesResultMap = new RestTemplate().exchange(sourcesUri.toString(), HttpMethod.GET, sourcesEntity, Object.class);

                if (sourcesResultMap.getStatusCodeValue() == 200) {
                    resultMap = sourcesResultMap;

                    rsalEntity = requestSerpApiLogService.success(rsalEntity, jsonInString);
                    rsalEntity.setRslPageToken(pageToken);
                    rsalEntity.setRslUrl(sourcesUrl);
                    requestSerpApiLogService.save(rsalEntity);
                }
            }

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "images_results").replace("image_sources", "images_results").replace("image_results", "images_results").replace("inline_images", "images_results");
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);

                    rsalEntity = requestSerpApiLogService.success(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                } else {
                    rsalEntity = requestSerpApiLogService.fail(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                }
            } else {
                rsalEntity = requestSerpApiLogService.fail(rsalEntity, resultMap.toString());
                requestSerpApiLogService.save(rsalEntity);
            }

            return results != null ? results : new ArrayList<>();
        }catch(Exception e){
            log.error(e.getMessage());

            if(rsalEntity != null) {
                requestSerpApiLogService.fail(rsalEntity, e.getMessage());
                requestSerpApiLogService.save(rsalEntity);
            }
        }
        return null;
    }

    /*
    public SearchInfoEntity searchResultAllTimeEntity(SearchInfoEntity searchResultEntity) {
        //검색 작업 엔티티 추출
        SearchInfoEntity sie = searchService.getSearchResultAllTimeEntity(searchResultEntity);

        //검색 작업 엔티티 기본값 세팅

        return sie;
    }
    */

    private String getUrl(SerpServicesEntity ssInfo, SearchInfoEntity searchInfoEntity, String tsiKeyword, NationCodeEntity ncInfo, int pageNo, int cntNation, boolean isText, String tsrSns){
        String url = "";
//        if(isText) {
        switch (searchInfoEntity.getTsiType()){
            case CommonCode.searchTypeKeyword -> {
                switch (ssInfo.getSsName()) {
                    case CommonCode.SerpAPIEngineGoogle -> { url = searchTextService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYoutube -> { if(pageNo == 0) url = searchYoutubeService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase()); }
                    case CommonCode.SerpAPIEngineBaidu -> {
                        if (cntNation == 0) { url = searchTextBaiduService.getUrl(tsrSns, tsiKeyword, pageNo); }
                    }
                    case CommonCode.SerpAPIEngineBing -> {
                        if (!ncInfo.getNcCode().equals("cn") && !ncInfo.getNcCode().equals("th") && !ncInfo.getNcCode().equals("ru") && !ncInfo.getNcCode().equals("vn")) {
                            url = searchTextBingService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo);
                        }
                    }
                    case CommonCode.SerpAPIEngineDuckduckgo -> { url = searchTextDuckduckgoService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYahoo -> { url = searchTextYahooService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYandex -> {
                        if (!ncInfo.getNcCode().equals("vn")) { url = searchTextYandexService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    }
                    case CommonCode.SerpAPIEngineNaver -> {
                        if (cntNation == 0) { url = searchTextNaverService.getUrl(tsrSns, tsiKeyword, pageNo); }
                    }
                }
            }
            case CommonCode.searchTypeImage -> {
                switch (ssInfo.getSsName()) {
                    case CommonCode.SerpAPIEngineGoogleReverseImage -> { url = searchTextService.getRerverseImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineGoogleLens -> { if(pageNo == 0) url = searchTextService.getImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), null, CommonCode.SerpAPIEngineGoogleLens); }
                    case CommonCode.SerpAPIEngineYandexImage -> { if (!ncInfo.getNcCode().equals("vn")) { url = searchTextService.getYandexImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), pageNo); } }
                }
            }
            case CommonCode.searchTypeKeywordImage -> {
                switch (ssInfo.getSsName()) {
                    case CommonCode.SerpAPIEngineGoogle -> { url = searchTextService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYoutube -> { if(pageNo == 0) url = searchYoutubeService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase());  }
                    case CommonCode.SerpAPIEngineBaidu -> {
                        if (cntNation == 0) { url = searchTextBaiduService.getUrl(tsrSns, tsiKeyword, pageNo); }
                    }
                    case CommonCode.SerpAPIEngineBing -> {
                        if (!ncInfo.getNcCode().equals("cn") && !ncInfo.getNcCode().equals("th") && !ncInfo.getNcCode().equals("ru") && !ncInfo.getNcCode().equals("vn")) {
                            url = searchTextBingService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo);
                        }
                    }
                    case CommonCode.SerpAPIEngineDuckduckgo -> { url = searchTextDuckduckgoService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYahoo -> { url = searchTextYahooService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineYandex -> {
                        if (!ncInfo.getNcCode().equals("vn")) { url = searchTextYandexService.getUrl(tsrSns, tsiKeyword, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    }
                    case CommonCode.SerpAPIEngineNaver -> {
                        if (cntNation == 0) { url = searchTextNaverService.getUrl(tsrSns, tsiKeyword, pageNo); }
                    }
                    case CommonCode.SerpAPIEngineGoogleReverseImage -> { url = searchTextService.getRerverseImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), pageNo); }
                    case CommonCode.SerpAPIEngineGoogleLens -> { if(pageNo == 0) url = searchTextService.getImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), null, CommonCode.SerpAPIEngineGoogleLens); }
                    case CommonCode.SerpAPIEngineYandexImage -> { if (!ncInfo.getNcCode().equals("vn")) { url = searchTextService.getYandexImageUrl(searchInfoEntity, ncInfo.getNcCode().toLowerCase(), pageNo); } }
                }
            }
        }

        return url;
    }
}