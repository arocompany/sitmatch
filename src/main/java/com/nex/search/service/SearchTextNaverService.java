package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByText;
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
public class SearchTextNaverService {
    private final ImageService imageService;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private String nationCode = "";
    private final SitProperties sitProperties;

    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode, String tsrSns){
        this.nationCode = nationCode;
        String textGl = this.nationCode;

        searchSnsByText(tsrSns, insertResult, searchInfoDto, textGl);
    }

    public void searchSnsByText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String textGl) {
        int index=0;

        searchByText(index, textGl, tsrSns, insertResult, searchInfoDto);
    }

    public void searchByText(int index, String textGl, String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){
        log.info("google keyword index = {}, textGl = {}, tsrSns = {}, loop = {}", index, textGl, tsrSns, loop);
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        //serpApi를 통하여 검색
                        return searchText(index, searchInfoDto, tsrSns, textGl, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        log.info("r == {}", r);
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
                }).thenRun(()-> {
                    // 검색결과가 있으면 다음 페이지 진입 로직
                    if(loop == true) {
                        CompletableFutureByText(index, tsrSns, textGl, insertResult, searchInfoDto);
                    }else{
                        log.info("==== CompletableFutureByText 함수 종료 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, textGl);
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchText(int index, SearchInfoDto searchInfoDto, String tsrSns, String textGl, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        try {
            if (CommonCode.snsTypeInstagram.equals(tsrSns)) { tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeFacebook.equals(tsrSns)) { tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeTwitter.equals(tsrSns)) { tsiKeywordHiddenValue = "트위터 " + tsiKeywordHiddenValue; }
            
            // serpAPI url 생성
//            String url = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), tsiKeywordHiddenValue, textGl, sitProperties.getTextNocache(), sitProperties.getTextLocation(), (index * 10), configData.getSerpApiKey()
//                    , null, "google", null);

            String url = sitProperties.getTextUrl()
                        + "?engine=naver"
                        + "&query="+tsiKeywordHiddenValue
                        + "&api_key=" + configData.getSerpApiKey()
                        + "&page="+(index+1)*10
                        + "&vc="+textGl
                        + "&safe=off";

            log.info("keyword === {}, url === {}", tsiKeywordHiddenValue, url);

            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
            List<RESULT> results = null;

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "images_results");
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);
                }
            }

            if (results == null || index >= sitProperties.getTextCountLimit() - 1) {
                loop = false;
            }
            return results != null ? results : new ArrayList<>();
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public <RESULT> List<SearchResultEntity> save(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) throws Exception {

        // 검색결과가 없으면 false처리 후 return
        if (results == null) {
            loop = false;
            return null;
        }

        List<SearchResultEntity> sreList = new ArrayList<>();

        for (RESULT result : results) {
            log.info("result item === {}", result);
            try {
                // original값이 없으면 thumbnail값 적용
                String imageUrl = getOriginalFn.apply(result);

                if(imageUrl == null) {
                    imageUrl = getThumbnailFn.apply(result);
                }

                if(imageUrl != null) {
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultTextEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    //sre.setTsrSerpEngine("Baidu");

                    int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
                    if(cnt > 0) {
                        log.info("file cnt === {}", cnt);
                    }else {
                        //이미지 파일 저장
                        imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn, false);
                        CommonStaticSearchUtil.setSearchResultDefault(sre);
                        searchResultRepository.save(sre);
                        sreList.add(sre);
                    }
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

    public String saveImgSearch(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        if (result == null) {
            loop=false;
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

    public void CompletableFutureByText(int index, String tsrSns, String textGl, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){
        log.info("==== CompletableFutureByText(재귀 함수 진입 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, textGl);
        if(!loop){
            return;
        }

        index++;
        int finalIndex = index;

        CompletableFuture.supplyAsync(() -> {
            try {
                // text기반 검색
                return searchText(finalIndex, searchInfoDto, tsrSns, textGl, SerpApiTextResult.class, SerpApiTextResult::getError, SerpApiTextResult::getImages_results);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return null;
            }
        }).thenApply((r) -> {
            try {
                if (r == null) {
                    loop = false;
                }
                // 결과 저장.(이미지)
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
            try {
                if (r != null) {
                    // 검색을 통해 결과 db에 적재.
                    saveImgSearch(r, insertResult);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).thenRun(()->{
            if(loop == true){
                CompletableFutureByText(finalIndex, tsrSns, textGl, insertResult,searchInfoDto);
            }else{
                log.info("==== CompletableFutureByText(재귀 함수 종료 ==== index 값: {} sns 값: {} textGl {}", finalIndex, tsrSns, textGl);
            }
        });

        // results.get();

    }
}
