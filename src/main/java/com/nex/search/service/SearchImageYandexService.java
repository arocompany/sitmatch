package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.ConfigData;
import com.nex.common.ConfigDataManager;
import com.nex.common.SitProperties;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.RequestSerpApiLogEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByImageForYandex;
import com.nex.search.entity.result.SerpApiImageResultForYandex;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
@Lazy
public class SearchImageYandexService {
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final ImageService imageService;
    private final RequestSerpApiLogService requestSerpApiLogService;

    private String nationCode = "";
    private final SitProperties sitProperties;
//    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public void search(SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode){
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        String tsrSns = "11";
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = configData.getHostImageUrl() + searchImageUrl.substring(searchImageUrl.indexOf("/" + sitProperties.getFileLocation3()) + 1);
        this.nationCode = nationCode;

        searchSnsByImage(searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchSnsByImage(String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {
        int index=0;

        String finalTextGl1 = this.nationCode;
        searchByImage(index, finalTextGl1, searchImageUrl, searchInfoDto, tsrSns, insertResult);
    }

    public void searchByImage(int index, String finalTextGl1, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, SearchInfoEntity insertResult) {

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 검색
                        return search(index, finalTextGl1,searchImageUrl,searchInfoDto, tsrSns, SerpApiImageResultForYandex.class, SerpApiImageResultForYandex::getError, SerpApiImageResultForYandex::getImage_results, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
//                        loop = false;
                        return null;
                    }
                }).thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return save(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImageForYandex::getOriginal_image
                                , Images_resultsByImageForYandex::getThumbnail
                                , Images_resultsByImageForYandex::getTitle
                                , Images_resultsByImageForYandex::getSource
                                , Images_resultsByImageForYandex::isFacebook
                                , Images_resultsByImageForYandex::isInstagram
                                , Images_resultsByImageForYandex::isTwitter
                                , finalTextGl1
                                , "yandex_images"
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                }).thenApplyAsync((r) -> {
                    try {// db에 적재.
                        if(r == null){ return null; }
                        return saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
//                .thenRun(()->{
//                    if(loop == true){
//                        CompletableFutureByImage(index, finalTextGl1,searchImageUrl,searchInfoDto, tsrSns,insertResult);
//                    }
//                })
        ;
    }

    public <INFO, RESULT> List<RESULT> search(int index,String finalTextGl1, String searchImageUrl, SearchInfoDto searchInfoDto, String tsrSns, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, SearchInfoEntity siEntity) throws Exception {
        int rsalUno = 0;
        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            String txtNation = "";
            switch (finalTextGl1){
                case "kr" -> txtNation = "135";
                case "us" -> txtNation = "84";
                case "cn" -> txtNation = "134";
                case "nl" -> txtNation = "118";
                case "th" -> txtNation = "995";
                case "ru" -> txtNation = "225";
            }
            String url = sitProperties.getTextUrl()
                    + "?lr=" + txtNation
                    + "&engine=yandex_images"
                    + "&url=" + searchImageUrl
                    + "&p="+index
                    + "&api_key=" + configData.getSerpApiKey();

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(siEntity.getTsiUno(), url, finalTextGl1, "yandex_images", null, index, configData.getSerpApiKey(), searchImageUrl);
            requestSerpApiLogService.save(rsalEntity);
            rsalUno = rsalEntity.getRslUno();


            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

            List<RESULT> results = null;

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String jsonInString = mapper.writeValueAsString(resultMap.getBody());
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);

                    rsalEntity = requestSerpApiLogService.success(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);

                    if(results.size() > 0) {
                        Integer limit = sitProperties.getTextCountLimit();
                        if(limit == null) limit = 10;
                        if (index < limit) {
                            searchByImage(index, finalTextGl1, searchImageUrl, searchInfoDto, tsrSns, siEntity);
                        }
                    }
                }else{
                    rsalEntity = requestSerpApiLogService.fail(rsalEntity, jsonInString);
                    requestSerpApiLogService.save(rsalEntity);
                }
            }else{
                rsalEntity = requestSerpApiLogService.fail(rsalEntity, resultMap.toString());
                requestSerpApiLogService.save(rsalEntity);
            }

//            if (results == null || index >= sitProperties.getTextCountLimit() - 1) {
//                loop = false;
//            }
            return results != null ? results : new ArrayList<>();
        }catch(Exception e){
            log.error(e.getMessage());

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.select(rsalUno);
            if(rsalEntity != null) {
                requestSerpApiLogService.fail(rsalEntity, e.getMessage());
                requestSerpApiLogService.save(rsalEntity);
            }
        }
        return null;
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
        SearchInfoEntity updateResult = searchInfoRepository.save(insertResult);

        List<SearchResultEntity> searchResultEntity = result;

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

//    public void CompletableFutureByImage(int index, String finalTextGl1, String searchImageUrl,SearchInfoDto searchInfoDto,  String tsrSns, SearchInfoEntity insertResult) {
//        log.info("==== CompletableFutureByImage(재귀 함수 진입 ==== index 값: {} sns 값: {} textGl {}", index, tsrSns, finalTextGl1);
//        if(!loop){
//            return;
//        }
//
//        index++;
//        int finalIndex = index;
//
//        // 이미지
//        CompletableFuture
//                .supplyAsync(() -> {
//                    try {
//                        // text기반 검색
//                        return search(finalIndex, finalTextGl1, searchImageUrl, searchInfoDto,tsrSns, SerpApiImageResultForYandex.class, SerpApiImageResultForYandex::getError, SerpApiImageResultForYandex::getImage_results, insertResult);
//                    } catch (Exception e) {
//                        loop = false;
//                        log.error(e.getMessage(), e);
//                        return null;
//                    }
//                })
//                .thenApply((r) -> {
//                    try {
//                        if (r == null) {
//                            loop = false;
//                        }
//                        // 결과 저장.(이미지)
//                        return save(
//                                r
//                                , tsrSns
//                                , insertResult
//                                , Images_resultsByImageForYandex::getOriginal_image
//                                , Images_resultsByImageForYandex::getThumbnail
//                                , Images_resultsByImageForYandex::getTitle
//                                , Images_resultsByImageForYandex::getSource
//                                , Images_resultsByImageForYandex::isFacebook
//                                , Images_resultsByImageForYandex::isInstagram
//                                , Images_resultsByImageForYandex::isTwitter
//                        );
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                        return null;
//                    }
//                }).thenApplyAsync((r) -> {
//                    try {
//                        // yandex검색을 통해 결과 db에 적재.
//                        if(r==null){ return null; }
//                        return saveImgSearch(r, insertResult);
//                    } catch (Exception e) {
//                        log.error(e.getMessage(), e);
//                        return null;
//                    }
//                }).thenRun(()->{
//                    if(loop == true){
//                        CompletableFutureByImage(finalIndex, finalTextGl1,searchImageUrl,searchInfoDto, tsrSns,insertResult);
//                    }
//                });
//    }

    public <RESULT> List<SearchResultEntity> save(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, Map<String, Object>> getOriginalFn, Function<RESULT, Map<String, Object>> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn, String nationCode, String engine) throws Exception {

        if (results == null) {
            log.info("result null");
            return null;
        }

        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();

        for (RESULT result : results) {
            try {
                //검색 결과 엔티티 추출
                SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultYandexReverseEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                if (!tsrSns.equals(sre.getTsrSns())) {
                    continue;
                }

//                int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
//                if (cnt > 0) {
//                    log.info("file cnt === {}", cnt);
//                } else {
                    //이미지 파일 저장
                    imageService.saveYandexReverseImageFile(insertResult.getTsiUno(), restTemplate, sre, result, getOriginalFn, getThumbnailFn, false);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    sre.setTsrNationCode(nationCode);
                    sre.setTsrEngine(engine);
                    searchResultRepository.save(sre);

                    sreList.add(sre);
//                }
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
