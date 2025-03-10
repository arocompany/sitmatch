package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.RequestSerpApiLogEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByText;
import com.nex.search.entity.result.SerpApiTextResult;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchTextYandexService {
    private final ImageService imageService;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final RequestSerpApiLogService requestSerpApiLogService;
    private String nationCode = "";
    private final SitProperties sitProperties;

//    private Boolean loop = true;
    private final RestTemplate restTemplate;
    private final RestTemplateConfig restTemplateConfig;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode, String tsrSns) {
        this.nationCode = nationCode;
        String textGl = this.nationCode;

        searchSnsByText(tsrSns, insertResult, searchInfoDto, textGl);
    }

    public void searchSnsByText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String textGl) {
        int index = 0;

        searchByText(index, textGl, tsrSns, insertResult, searchInfoDto);
    }

    public void searchByText(int index, String textGl, String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        //serpApi를 통하여 검색
                        return searchText(index, searchInfoDto, tsrSns, textGl, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        //검색 결과를 SearchResult Table에 저장 및 이미지 저장
                        return save(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByText::getOriginal
                                , Images_resultsByText::getThumbnail
                                , Images_resultsByText::getTitle
                                , Images_resultsByText::getLink
                                , Images_resultsByText::isFacebook
                                , Images_resultsByText::isInstagram
                                , Images_resultsByText::isTwitter
                                , textGl
                                , "yandex"
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenAccept((r) -> {
                    try {
                        // 검색 결과 상태값을 완료로 변경 및 SearchJob Table에 Insert
                        saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                })
//                .thenRun(() -> {
//                    // 검색결과가 있으면 다음 페이지 진입 로직
//                    if (loop == true) {
//                        CompletableFutureByText(index, tsrSns, textGl, insertResult, searchInfoDto);
//                    } else {
//                        log.info("==== CompletableFutureByText 함수 종료 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, textGl);
//                    }
//                })
        ;
    }

    public String getUrl(String tsrSns, String tsiKeywordHiddenValue, String textGl, int index) {
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        if (Consts.INSTAGRAM.equals(tsrSns)) {
            tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue;
        } else if (Consts.FACEBOOK.equals(tsrSns)) {
            tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue;
        } else if (Consts.TWITTER.equals(tsrSns)) {
            tsiKeywordHiddenValue = "트위터 " + tsiKeywordHiddenValue;
        }

        String txtNation = "";
        switch (textGl) {
            case "kr" -> txtNation = "135";
            case "us" -> txtNation = "84";
            case "cn" -> txtNation = "134";
            case "nl" -> txtNation = "118";
            case "th" -> txtNation = "995";
            case "ru" -> txtNation = "225";
        }

        String url = sitProperties.getTextUrl()
                + "?engine=yandex"
                + "&text=" + tsiKeywordHiddenValue
                + "&api_key=" + configData.getSerpApiKey()
                + "&p=" + index
                + "&lr=" + txtNation;

        return url;
    }

    public <INFO, RESULT> List<RESULT> searchText(int index, SearchInfoDto searchInfoDto, String tsrSns, String textGl, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, SearchInfoEntity siEntity) throws Exception {
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        int rsalUno = 0;
        try {
            if (CommonCode.snsTypeInstagram.equals(tsrSns)) {
                tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue;
            } else if (CommonCode.snsTypeFacebook.equals(tsrSns)) {
                tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue;
            } else if (CommonCode.snsTypeTwitter.equals(tsrSns)) {
                tsiKeywordHiddenValue = "트위터 " + tsiKeywordHiddenValue;
            }

            String txtNation = "";
            switch (textGl) {
                case "kr" -> txtNation = "135";
                case "us" -> txtNation = "84";
                case "cn" -> txtNation = "134";
                case "nl" -> txtNation = "118";
                case "th" -> txtNation = "995";
                case "ru" -> txtNation = "225";
            }

            String url = sitProperties.getTextUrl()
                    + "?engine=yandex"
                    + "&text=" + tsiKeywordHiddenValue
                    + "&api_key=" + configData.getSerpApiKey()
                    + "&p=" + index
                    + "&lr=" + txtNation;

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(siEntity.getTsiUno(), url, textGl, "yandex", tsiKeywordHiddenValue, index, configData.getSerpApiKey(), null);
            requestSerpApiLogService.save(rsalEntity);
            rsalUno = rsalEntity.getRslUno();

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = restTemplateConfig.customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
            List<RESULT> results = null;

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "images_results");
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);

                    rsalEntity = requestSerpApiLogService.success(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);

                    if(results.size() > 0) {
                        Integer limit = sitProperties.getTextCountLimit();
                        if(limit == null) limit = 10;
                        if (index < limit) {
                            searchByText( index + 1, textGl, tsrSns, siEntity, searchInfoDto);
                        }
                    }
                } else {
                    rsalEntity = requestSerpApiLogService.fail(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                }
            } else {
                rsalEntity = requestSerpApiLogService.fail(rsalEntity, resultMap.toString());
                requestSerpApiLogService.save(rsalEntity);
            }

//            if (results == null || index >= sitProperties.getTextCountLimit() - 1) {
//                loop = false;
//            }
            return results != null ? results : new ArrayList<>();
        } catch (Exception e) {
            log.error(e.getMessage());

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.select(rsalUno);
            if (rsalEntity != null) {
                requestSerpApiLogService.fail(rsalEntity, e.getMessage());
                requestSerpApiLogService.save(rsalEntity);
            }
        }
        return null;
    }
    public <RESULT> List<SearchResultEntity> save(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn, String nationCode, String engine) throws Exception {

        // 검색결과가 없으면 false처리 후 return
        if (results == null) {
//            loop = false;
            return null;
        }

        List<SearchResultEntity> sreList = new ArrayList<>();
//        results = results.stream().distinct().toList();
        // 중복 제거를 위한 Map
        Map<String, Object> uniqueResults = new HashMap<>();
        Thread.sleep(1000);
        List<String> tempList = searchResultRepository.findDistinctSiteUrlsByTsiUno(insertResult.getTsiUno());

        for(String item: tempList){
            uniqueResults.put(item, null);
        }
        for (RESULT result : results) {
            try {
                String siteUrl = getLinkFn.apply(result); // siteUrl 가져오기

                // siteUrl이 중복되지 않는 경우만 추가
                if (!uniqueResults.containsKey(siteUrl)) {
                    uniqueResults.put(siteUrl, result);
                    String imageUrl = getOriginalFn.apply(result) != null ? getOriginalFn.apply(result) : getThumbnailFn.apply(result);
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultTextEntity(insertResult.getTsiUno(), tsrSns, result, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);
                    if (StringUtils.hasText(imageUrl)) {
                        try {
                            if (!tsrSns.equals(sre.getTsrSns())) {
                                continue;
                            }
                            //이미지 파일 저장
                            imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getThumbnailFn, false);

                        } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                            log.error(e.getMessage());
                            throw new IOException(e);
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                    }

                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    sre.setTsrNationCode(nationCode);
                    sre.setTsrEngine(engine);
                    searchResultRepository.save(sre);
                    sreList.add(sre);
                }
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        return sreList;
    }

    public String saveImgSearch(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        if (result == null) {
//            loop = false;
            return null;
        }
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        CommonStaticSearchUtil.setSearchInfoDefault_2(insertResult);
        searchInfoRepository.save(insertResult);
        List<SearchResultEntity> searchResultEntity = result;

        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = CommonStaticSearchUtil.getSearchJobEntity(sre);
                CommonStaticSearchUtil.setSearchJobDefault(sje);
                searchJobRepository.save(sje);
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

//    public void CompletableFutureByText(int index, String tsrSns, String textGl, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
//        log.info("==== CompletableFutureByText(재귀 함수 진입 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, textGl);
//        if (!loop) {
//            return;
//        }
//
//        index++;
//        int finalIndex = index;
//
//        CompletableFuture.supplyAsync(() -> {
//            try {
//                // text기반 검색
//                return searchText(finalIndex, searchInfoDto, tsrSns, textGl, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results, insertResult);
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                return null;
//            }
//        }).thenApply((r) -> {
//            try {
//                if (r == null) {
//                    loop = false;
//                }
//                // 결과 저장.(이미지)
//                return save(
//                        r
//                        , tsrSns
//                        , insertResult
//                        , Images_resultsByText::getOriginal
//                        , Images_resultsByText::getThumbnail
//                        , Images_resultsByText::getTitle
//                        , Images_resultsByText::getLink
//                        , Images_resultsByText::isFacebook
//                        , Images_resultsByText::isInstagram
//                        , Images_resultsByText::isTwitter
//                );
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                return null;
//            }
//        }).thenAccept((r) -> {
//            try {
//                if (r != null) {
//                    // 검색을 통해 결과 db에 적재.
//                    saveImgSearch(r, insertResult);
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//            }
//        }).thenRun(() -> {
//            if (loop == true) {
//                CompletableFutureByText(finalIndex, tsrSns, textGl, insertResult, searchInfoDto);
//            } else {
//                log.info("==== CompletableFutureByText(재귀 함수 종료 ==== index 값: {} sns 값: {} textGl {}", finalIndex, tsrSns, textGl);
//            }
//        });
//    }
}
