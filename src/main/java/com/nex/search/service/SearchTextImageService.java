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
import com.nex.search.entity.result.Images_resultsByImage;
import com.nex.search.entity.result.Images_resultsByText;
import com.nex.search.entity.result.SerpApiImageResult;
import com.nex.search.entity.result.SerpApiTextResult;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchTextImageService {
    private final ImageService imageService;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final RequestSerpApiLogService requestSerpApiLogService;
    private Boolean loop = true;
    private final RestTemplate restTemplate;
    private String nationCode = "";
    private final SitProperties sitProperties;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode, String tsrSns){
//        String tsrSns = "17";
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = configData.getHostImageUrl() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);
        this.nationCode = nationCode;
        searchSnsByImage(searchImageUrl, tsiKeywordHiddenValue, searchInfoDto, tsrSns, insertResult);

    }

    public void searchSnsByImage(String searchImageUrl, String tsiKeywordHiddenValue, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        int index=0;
        String textGl = this.nationCode;

        searchByImage(index, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, insertResult);
        searchByText(index, textGl,tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchByImage(int index, String textGl, String tsiKeywordHiddenValue, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return search(index, textGl, tsiKeywordHiddenValue, searchImageUrl,searchInfoDto, tsrSns, SerpApiImageResult.class, SerpApiImageResult::getError, SerpApiImageResult::getInline_images, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try { // 결과 저장.(이미지)
                        return saveImage(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImage::getOriginal
                                , Images_resultsByImage::getThumbnail
                                , Images_resultsByImage::getTitle
                                , Images_resultsByImage::getSource
                                , Images_resultsByImage::isFacebook
                                , Images_resultsByImage::isInstagram
                                , Images_resultsByImage::isTwitter
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try { // db에 적재.
                        if(r == null) { return null; }
                        return saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenRun(()->{
                    if(loop == true){
                        log.info("loop == true 진입: " + loop);
                        CompletableFutureByImage(index, textGl,tsiKeywordHiddenValue, searchImageUrl,searchInfoDto, tsrSns,insertResult);
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> search(int index, String textGl, String tsiKeywordHiddenValue, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, SearchInfoEntity siEntity) throws Exception {
        int rsalUno = 0;
        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            if (CommonCode.snsTypeInstagram.equals(tsrSns)) { tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeFacebook.equals(tsrSns)) { tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeTwitter.equals(tsrSns)){ tsiKeywordHiddenValue = "트위터 " + tsiKeywordHiddenValue; }

            String url = sitProperties.getTextUrl()
                    + "?gl=" + textGl
                    + "&no_cache=" + sitProperties.getTextNocache()
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + configData.getSerpApiKey()
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + String.valueOf(index * 10)
                    // + "&tbm=" + textTbm
                    + "&engine=google_reverse_image"
                    + "&image_url=" + searchImageUrl;

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(siEntity.getTsiUno(), url, textGl, "google_reverse_image", tsiKeywordHiddenValue, index, configData.getSerpApiKey(), searchImageUrl);
            requestSerpApiLogService.save(rsalEntity);
            rsalUno = rsalEntity.getRslUno();

            log.info("search 진입");
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

            List<RESULT> results = null;

//            log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "images_results");
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);

                    rsalEntity = requestSerpApiLogService.success(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                }else{
                    rsalEntity = requestSerpApiLogService.fail(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                }
            }else{
                rsalEntity = requestSerpApiLogService.fail(rsalEntity, resultMap.toString());
                requestSerpApiLogService.save(rsalEntity);
            }

            if (results == null || index >= sitProperties.getTextCountLimit() - 1) {
                loop = false;
            }
            //        if(index>2){ loop=false; }

//            log.info("results: " + results);
//            log.debug("search loop: " + loop);

            return results != null ? results : new ArrayList<>();
        }catch (Exception e){
            log.error(e.getMessage());

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.select(rsalUno);
            if(rsalEntity != null) {
                requestSerpApiLogService.fail(rsalEntity, e.getMessage());
                requestSerpApiLogService.save(rsalEntity);
            }
        }
        return null;
    }

    public <RESULT> List<SearchResultEntity> save(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) throws Exception {
        log.info("========= save 진입 =========");

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
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultTextEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn,false);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    searchResultRepository.save(sre);
//                    searchService.saveSearchResult(sre);

                    sreList.add(sre);
                }
            } catch (IOException e) { // IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }

    public String saveImgSearch(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        insertResult.setTsiStat("13");

        if (result == null) {
            loop = false;
            return null;
        }

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
//        SearchInfoEntity updateResult = searchService.saveSearchInfo_2(insertResult);
        CommonStaticSearchUtil.setSearchInfoDefault_2(insertResult);
        searchInfoRepository.save(insertResult);

        List<SearchResultEntity> searchResultEntity = result;

        //SearchJobEntity sje = null;
        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = CommonStaticSearchUtil.getSearchJobEntity(sre);
//                searchService.saveSearchJob(sje);
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

    public void CompletableFutureByImage(int index, String textGl, String tsiKeywordHiddenValue, String searchImageUrl, SearchInfoDto searchInfoDto,  String tsrSns, SearchInfoEntity insertResult) {
        index++;
        int finalIndex = index;

        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return search(finalIndex, textGl,tsiKeywordHiddenValue, searchImageUrl, searchInfoDto,tsrSns, SerpApiImageResult.class, SerpApiImageResult::getError, SerpApiImageResult::getInline_images, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try { // 결과 저장.(이미지)
                        return saveImage(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImage::getOriginal
                                , Images_resultsByImage::getThumbnail
                                , Images_resultsByImage::getTitle
                                , Images_resultsByImage::getSource
                                , Images_resultsByImage::isFacebook
                                , Images_resultsByImage::isInstagram
                                , Images_resultsByImage::isTwitter
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try { // 검색을 통해 결과 db에 적재.
                        if(r == null) return null;
                        return saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenRun(()->{
                    if(loop == true){
                        CompletableFutureByImage(finalIndex, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, insertResult);
                    }
                });
    }

    // ----------------------------------------------------------------------------------------------------------- //

    public void searchByText(int index, String textGl, String tsiKeywordHiddenValue, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return search(index, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try { // 결과 저장.(이미지)
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
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenAccept((r) -> {
                    try { // 검색을 통해 결과 db에 적재.
                        saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }).thenRun(()->{
                    if(loop == true){
                        log.info("loop 값1: " + loop);
                        CompletableFutureText(index, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, insertResult);
                    }
                });
    }

    public void CompletableFutureText(int index, String textGl, String tsiKeywordHiddenValue,String searchImageUrl,SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        log.info("==== CompletableFutureText(재귀 함수 진입 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, textGl);
        if(!loop){
            return;
        }

        index++;
        int finalIndex = index;

        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return search(finalIndex, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try { // 결과 저장.(이미지)
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
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenAccept((r) -> {
                    try { // 검색을 통해 결과 db에 적재.
                        saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }).thenRun(()->{
                    if(loop == true){
                        log.info("loop 값2: " + loop);
                        CompletableFutureText(finalIndex, textGl, tsiKeywordHiddenValue, searchImageUrl, searchInfoDto, tsrSns, insertResult);
                    }
                });

    }


    public <RESULT> List<SearchResultEntity> saveImage(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) throws Exception {
        log.info("========= saveImage 진입 =========");

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
//                String imageUrl = getOriginalFn.apply(result) ;
//                log.info("imageUrl1: "+imageUrl);
//                if(imageUrl == null) {
//                    imageUrl = getThumbnailFn.apply(result);
//                }
//                log.info("imageUrl2: "+imageUrl);
//                if(imageUrl != null) {
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultGoogleReverseEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

                    int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
                    if(cnt > 0) {
                        log.info("file cnt === {}", cnt);
                    }else {
                        //이미지 파일 저장
                        imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn, false);
                        CommonStaticSearchUtil.setSearchResultDefault(sre);
                        searchResultRepository.save(sre);
                        //searchService.saveSearchResult(sre);

                        sreList.add(sre);
                    }
//                }
            } catch (IOException e) { // IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }
}
