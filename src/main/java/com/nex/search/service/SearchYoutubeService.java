package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.YoutubeByResult;
import com.nex.search.entity.result.Youtube_resultsByText;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
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
@Configuration
@RequiredArgsConstructor
public class SearchYoutubeService {
    private final ImageService imageService;

    private final RestTemplateConfig restTemplateConfig;

    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;

    private final SitProperties sitProperties;

    public void searchYoutube(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String nationCode){
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();
        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();
        try {

            String url = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), tsiKeywordHiddenValue, nationCode, null, null, null, configData.getSerpApiKey(), null, "youtube", null);
            log.info("youtube keyword === {}, url === {}", tsiKeywordHiddenValue, url);
            CompletableFutureYoutubeByResult(url, tsrSns, insertResult);
        } catch (Exception e){
            log.debug("Exception: "+e);
        }
    }

    public void CompletableFutureYoutubeByResult(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 검색
                        return searchByYoutube(url, YoutubeByResult.class, YoutubeByResult::getError, YoutubeByResult::getVideo_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYoutube(
                                r
                                , tsrSns
                                , insertResult
                                , Youtube_resultsByText::getPosition_on_page
                                , Youtube_resultsByText::getLink
                                , Youtube_resultsByText::getTitle
                                , Youtube_resultsByText::getThumbnail
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApplyAsync((r) -> {
                    try {
                        if(r == null) return null;
                        // 검색을 통해 결과 db에 적재.
                        return saveImgSearch(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }

    // 유튜브
    public <INFO, RESULT> List<RESULT> searchByYoutube(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        try {
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = restTemplateConfig.customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
            List<RESULT> results = null;

            if (resultMap.getStatusCodeValue() == 200) {
                ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                // String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "video_results");
                String jsonInString = mapper.writeValueAsString(resultMap.getBody());
                INFO info = mapper.readValue(jsonInString, infoClass);

                if (getErrorFn.apply(info) == null) {
                    results = getResultFn.apply(info);
                }
            }else{
                log.error(resultMap.getStatusCode() + "");
            }
            return results != null ? results : new ArrayList<>();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    // ,Function<RESULT, String> getThumnailFn
    public <RESULT> List<SearchResultEntity> saveYoutube(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getPositionFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getTitleFn ,Function<RESULT, Map<String,String>> getThumnailFn) throws Exception {
        if (results == null) {
            log.info("result null");
            return null;
        }
        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();
        //SearchResultEntity sre = null;
        for (RESULT result : results) {
            try {
                //검색 결과 엔티티 추출
                SearchResultEntity sre = getYoutubeResultEntity(insertResult.getTsiUno(), result, getPositionFn,getLinkFn, getTitleFn);

                //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                if (!tsrSns.equals(sre.getTsrSns())) {
                    continue;
                }

                //이미지 파일 저장
                imageService.saveYoutubeImageFile(insertResult.getTsiUno(), restTemplateConfig.customRestTemplate(), sre, result, getThumnailFn);
                saveSearchResult(sre);

                sreList.add(sre);

            } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }

    /**
     * 검색 작업 저장
     *
     * @param result       (검색 결과 엔티티 List)
     * @param insertResult (검색 이력 엔티티)
     * @return String       (저장 결과)
     */
    public String saveImgSearch(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        if (result == null) {
            return null;
        }
        insertResult.setTsiStat(CommonCode.searchStateFinish);

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
        // SearchInfoEntity updateResult = saveSearchInfo_2(insertResult);
        saveSearchInfo_2(insertResult);
        List<SearchResultEntity> searchResultEntity = result;

        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = CommonStaticSearchUtil.getSearchJobEntity(sre);
                saveSearchJob(sje);
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

    public <RESULT> SearchResultEntity getYoutubeResultEntity(int tsiUno, RESULT result
            , Function<RESULT, String> getPositionFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getTitleFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getPositionFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSns(CommonCode.snsTypeGoogle);
        return sre;
    }

    public SearchResultEntity saveSearchResult(SearchResultEntity sre) {
        CommonStaticSearchUtil.setSearchResultDefault(sre);
        return searchResultRepository.save(sre);
    }
    public SearchInfoEntity saveSearchInfo_2(SearchInfoEntity sie) {
        CommonStaticSearchUtil.setSearchInfoDefault_2(sie);
        return searchInfoRepository.save(sie);
    }

    public SearchJobEntity saveSearchJob(SearchJobEntity sje) {
        CommonStaticSearchUtil.setSearchJobDefault(sje);
        return searchJobRepository.save(sje);
    }
}
