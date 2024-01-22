package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByText;
import com.nex.search.entity.result.YandexByTextResult;
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
public class SearchTextService {
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
        String textYandexGl = this.nationCode;
        // searchText(tsiType, insertResult, folder, tsrSns, searchInfoDto);
        searchSnsByText(tsrSns, insertResult, searchInfoDto, textYandexGl);
    }

    public void searchSnsByText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String textYandexGl) {
        int index=0;

        searchByText(index, textYandexGl, tsrSns, insertResult, searchInfoDto);
    }

    public void searchByText(int index, String textYandexGl, String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        //serpApi를 통하여 검색
                        return searchTextYandex(index, searchInfoDto, tsrSns, textYandexGl, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        //검색 결과를 SearchResult Table에 저장 및 이미지 저장
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
                        // 검색 결과 상태값을 완료로 변경 및 SearchJob Table에 Insert
                        saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }).thenRun(()-> {
                    if(loop == true) {
                        CompletableFutureYandexByText(index, tsrSns, textYandexGl, insertResult, searchInfoDto);
                    }else{
                        log.info("==== CompletableFutureYandexByText 함수 종료 ==== index 값: {} sns 값: {} textYandexGl {}", index, tsrSns, textYandexGl);
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchTextYandex(int index, SearchInfoDto searchInfoDto, String tsrSns, String textYandexGl, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("============== searchTextYandex index: {} sns: {} textYandexGl {}", index, tsrSns, textYandexGl);
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        try {
            if (CommonCode.snsTypeInstagram.equals(tsrSns)) { tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeFacebook.equals(tsrSns)) { tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue; }

            String url = CommonStaticSearchUtil.getSerpApiUrlForGoogle(sitProperties.getTextYandexUrl(), tsiKeywordHiddenValue, textYandexGl, sitProperties.getTextYandexNocache(), sitProperties.getTextYandexLocation(), (index * 10), configData.getSearchYandexTextApiKey()
                    , null, sitProperties.getImageYandexEngine());
            log.info("keyword === {}", tsiKeywordHiddenValue);
            log.info("url === {} " + url);

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
                log.info("searchTextYandex results: " + results);

            }

            if (results == null || index >= sitProperties.getTextYandexCountLimit() - 1) {
                loop = false;
            }
            return results != null ? results : new ArrayList<>();
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public <RESULT> List<SearchResultEntity> saveYandex(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws Exception {
        log.info("=========== saveYandex 진입 ==============");
        if (results == null) {
            loop = false;
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
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultTextEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn,false);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    searchResultRepository.save(sre);
//                    searchService.saveSearchResult(sre);

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
        if (result == null) {
            loop=false;
            return null;
        }
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }

//        SearchInfoEntity updateResult = searchService.saveSearchInfo_2(insertResult);
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

    public void CompletableFutureYandexByText(int index, String tsrSns, String textYandexGl, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto){
        log.info("==== CompletableFutureYandexByText(재귀 함수 진입 ==== index 값: {} sns 값: {} textYandexGl {}", index, tsrSns, textYandexGl);
        if(!loop){
            return;
        }

        index++;
        int finalIndex = index;

        CompletableFuture.supplyAsync(() -> {
            try {
                // text기반 yandex 검색
                return searchTextYandex(finalIndex, searchInfoDto, tsrSns, textYandexGl, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
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
                log.info("loop==true 진입:" + loop);
                CompletableFutureYandexByText(finalIndex, tsrSns, textYandexGl, insertResult,searchInfoDto);
            }else{
                log.info("==== CompletableFutureYandexByText(재귀 함수 종료 ==== index 값: {} sns 값: {} textYandexGl {}", finalIndex, tsrSns, textYandexGl);
            }

        });

        // results.get();

    }
}
