package com.nex.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.RequestSerpApiLogEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.result.CustomResult;
import com.nex.search.entity.result.CustomResults;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchImageGoogleLensService {
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;

    private final RequestSerpApiLogService requestSerpApiLogService;

    private final ImageService imageService;
    private String nationCode = "";
    private final SitProperties sitProperties;
    private final RestTemplate restTemplate;

    private final RestTemplateConfig restTemplateConfig;

    public void searchByGoogleLensImage(String tsrSns, SearchInfoEntity insertResult, String nationCode, String customTag) throws JsonProcessingException {
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        try {
            String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
            searchImageUrl = configData.getHostImageUrl() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);

            this.nationCode = nationCode;
            String finalTextGl1 = this.nationCode;

            String url = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), null, nationCode, null, searchImageUrl, "google_lens", customTag);

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(insertResult.getTsiUno(), url, finalTextGl1, "google_lens", null, null, configData.getSerpApiKey(), searchImageUrl);
            requestSerpApiLogService.save(rsalEntity);
            int rsalUno = rsalEntity.getRslUno();

            CompletableFutureGoogleLensByImage(url, tsrSns, insertResult, finalTextGl1, rsalUno, customTag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CompletableFutureGoogleLensByImage(String url, String tsrSns, SearchInfoEntity insertResult, String finalTextGl1, int rsalUno, String customTag) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchGoogleLens(url, CustomResult.class, CustomResult::getError, CustomResult::getResults, finalTextGl1, rsalUno, customTag);
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
                                , CustomResults::getTitle
                                , CustomResults::getLink
                                , CustomResults::getImage
                                , CustomResults::isFacebook
                                , CustomResults::isInstagram
                                , CustomResults::isTwitter
                                , finalTextGl1
                                , "google_lens"
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try { // 결과 db에 적재.
                        if (r == null) {
                            return null;
                        }
                        return saveImgSearchGoogleLens(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchGoogleLens(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, String finalTextGl1, int rsalUno, String customTag) throws Exception {
        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = restTemplateConfig.customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

            List<RESULT> results = null;

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.select(rsalUno);

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody());
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
    public <RESULT> List<SearchResultEntity> saveGoogleLens(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn
    , String nationCode, String engine) throws Exception {
        if (results == null) {
            log.info("result null");
            return null;
        }
        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();
//        results = results.stream().distinct().toList();
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
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultGoogleLensEntity(insertResult.getTsiUno(), tsrSns, result, getThumbnailFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

//                int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
//                if (cnt > 0) {
//                    log.info("file cnt === {}", cnt);
//                } else {
                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getThumbnailFn, true);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    sre.setTsrNationCode(nationCode);
                    sre.setTsrEngine(engine);
                    searchResultRepository.save(sre);
                    sreList.add(sre);
//                }
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
        if (result == null) {
            return null;
        }
        insertResult.setTsiStat(CommonCode.searchStateFinish);

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }

        CommonStaticSearchUtil.setSearchInfoDefault_2(insertResult);
        searchInfoRepository.save(insertResult);

        List<SearchResultEntity> searchResultEntity = result;

        if (searchResultEntity != null) {
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
        }
        return "저장 완료";
    }
}
