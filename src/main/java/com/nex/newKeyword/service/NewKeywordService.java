package com.nex.newKeyword.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.Consts;
import com.nex.search.entity.NewKeywordEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.result.Images_resultsByText;
import com.nex.search.entity.result.YandexByTextResult;
import com.nex.search.repo.NewKeywordRepository;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.service.ImageService;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewKeywordService {
    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final SearchJobRepository searchJobRepository;
    private final SearchService searchService;
    private final ImageService imageService;
    private final NewKeywordRepository newKeywordRepository;

    private final ResourceLoader resourceLoader;

    @Value("${file.location2}")
    private String fileLocation2;
    @Value("${python.video.module}")
    private String pythonVideoModule;
    @Value("${search.yandex.text.url}")
    private String textYandexUrl;
    @Value("${search.yandex.text.gl}")
    private String textYandexGl;
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

    private Boolean loop = true;
    private final RestTemplate restTemplate;

    public NewKeywordEntity addNewKeyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,String addNewKeyword) {
        NewKeywordEntity nke = new NewKeywordEntity();
        nke.setUserId(sessionInfoDto.getUserId());
        nke.setKeyword(addNewKeyword);
        nke.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        nke.setKeywordStus("0");

        return newKeywordRepository.save(nke);
    }

    public SearchInfoEntity saveNewKeywordSearchInfo(SearchInfoEntity sie) {
        setNewKeywordInfoDefault(sie);
        return searchInfoRepository.save(sie);
    }

    public static void setNewKeywordInfoDefault(SearchInfoEntity sie) {
        sie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setDataStatCd("10");
        sie.setSearchValue("1");
    }

    public void searchByText(String newKeyword, SearchInfoEntity insertResult, String folder) throws ExecutionException, InterruptedException {
        int index = 0;
        loop = true;
        String tsrSns = "11";
        // String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        do {
            String url = textYandexUrl
                    + "?q=" + newKeyword
                    + "&gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
//                    + "&tbm=" + textYandexTbm
                    + "&start=" + String.valueOf(index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;

        } while (loop);
        log.info("loop값3: " + loop);

    }

    public void CompletableFutureYandexByText(String url, String tsrSns, SearchInfoEntity insertResult) {
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchTextYandex(url, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
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
                })
                .thenAccept((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
    }

    public <INFO, RESULT> List<RESULT> searchTextYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        // HttpHeaders header = new HttpHeaders();
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
            // String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "organic_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
            log.info("mapper: "+ mapper);
            log.info("jsonInString: "+ jsonInString);
            log.info("info: "+ info);
            log.info("result: "+results);

        }
        return results != null ? results : new ArrayList<>();
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
                    SearchResultEntity sre = searchService.getSearchResultEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

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

    public String saveImgSearchYandex(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        insertResult.setTsiStat("13");

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

    // admin 일때
    public Map<String, Object> getNewKeywordInfoList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, pageRequest);
        //  Page<SearchInfoEntity> searchInfoListPage2 = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, pageRequest);
        Page<SearchInfoEntity> newKeywordInfoList = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10","1", keyword, pageRequest);

        outMap.put("newKeywordInfoList", newKeywordInfoList);
        outMap.put("totalPages", newKeywordInfoList.getTotalPages());
        outMap.put("number", newKeywordInfoList.getNumber());
        outMap.put("totalElements", newKeywordInfoList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    // admin 아닐때
    public Map<String, Object> getNewKeywordInfoList(Integer page, String keyword, int userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<SearchInfoEntity> newKeywordInfoList = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10", "1", keyword, userUno, pageRequest);

        outMap.put("newKeywordInfoList", newKeywordInfoList);
        outMap.put("totalPages", newKeywordInfoList.getTotalPages());
        outMap.put("number", newKeywordInfoList.getNumber());
        outMap.put("totalElements", newKeywordInfoList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

}
