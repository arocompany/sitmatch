package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.SitProperties;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByImage;
import com.nex.search.entity.result.YandexByImageResult;
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
public class SearchImageService {
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final ImageService imageService;

    private String nationCode = "";
    private final SitProperties sitProperties;
    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode){
        String tsrSns = "11";
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = sitProperties.getServerIp() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);
        // searchImageUrl= "http://106.254.235.202:9091/imagePath/requests/20240115/05b9343c-b1d2-48c6-ae3a-27dfd3bae972.jpg";
        this.nationCode = nationCode;

        searchSnsByImage(searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchSnsByImage(String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        int index=0;

        String finalTextYandexGl1 = this.nationCode;
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
                        loop = false;
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
                        if(r == null){ return null; }
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
        try {
            String url = sitProperties.getTextYandexUrl()
                    + "?gl=" + finalTextYandexGl1
                    + "&no_cache=" + sitProperties.getTextYandexNocache()
                    + "&api_key=" + sitProperties.getTextYandexApikey()
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + String.valueOf(index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + sitProperties.getImageYandexEngine()
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


            if (results == null || index >= sitProperties.getTextYandexCountLimit() - 1) {
                loop = false;
            }

            // if(index >1){ loop=false;}

            log.info("results: " + results);
            log.debug("searchYandex loop: " + loop);

            return results != null ? results : new ArrayList<>();
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return null;
    }


    public String saveImgSearchYandex(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        if (result == null) {
            loop = false;
            return null;
        }
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }

        CommonStaticSearchUtil.setSearchInfoDefault_2(insertResult);
        SearchInfoEntity updateResult = searchInfoRepository.save(insertResult);

        List<SearchResultEntity> searchResultEntity = result;


        //SearchJobEntity sje = null;
        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = CommonStaticSearchUtil.getSearchJobEntity(sre);
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

    public void CompletableFutureYandexByImage(int index, String finalTextYandexGl1, String searchImageUrl,SearchInfoDto searchInfoDto,  String tsrSns, SearchInfoEntity insertResult) {
        log.info("==== CompletableFutureYandexByImage(재귀 함수 진입 ==== index 값: {} sns 값: {} textYandexGl {}", index, tsrSns, finalTextYandexGl1);
        if(!loop){
            return;
        }

        index++;
        int finalIndex = index;

        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchYandex(finalIndex, finalTextYandexGl1, searchImageUrl, searchInfoDto,tsrSns, YandexByImageResult.class, YandexByImageResult::getError, YandexByImageResult::getInline_images);
                    } catch (Exception e) {
                        loop = false;
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        if (r == null) {
                            loop = false;
                        }
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
                        if(r==null){ return null; }
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
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultGoogleReverseEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn,false);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    searchResultRepository.save(sre);

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
}
