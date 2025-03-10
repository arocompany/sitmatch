package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.*;
import com.nex.search.entity.result.CustomResult;
import com.nex.search.entity.result.CustomResults;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.repo.VideoInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.nex.common.CmnUtil.execPython;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchVideoGoogleLensService {
    private final ImageService imageService;

    private final RestTemplateConfig restTemplateConfig;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchJobRepository searchJobRepository;
    private final SearchResultRepository searchResultRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SitProperties sitProperties;
    private final RequestSerpApiLogService requestSerpApiLogService;

    @Async
    public void searchByGoogleLensVideo(String tsrSns, SearchInfoEntity insertResult, String path, String nationCode, List<String> files, String lensType) throws Exception {
//        List<String> files = processVideo(insertResult);
        if(files == null || (files != null && files.isEmpty()))  { saveErrorInfo(insertResult);  return;}
//        for (int i = 0; i < files.size(); i++) {
//            VideoInfoEntity videoInfo = new VideoInfoEntity();
//            videoInfo.setTsiUno(insertResult.getTsiUno());
//            videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
//            videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
//            saveVideoInfo(videoInfo);
//        }

        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            for (int i = 0; i < files.size(); i++) {
                String searchImageUrl = configData.getHostImageUrl() + sitProperties.getFileLocation3() + "/" + path + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));

                int rsalUno = 0;
                try {
                    String url = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), null, nationCode, null, searchImageUrl, "google_lens", lensType);
                    RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(insertResult.getTsiUno(), url, nationCode, "google_lens", null, null, configData.getSerpApiKey(), searchImageUrl);
                    requestSerpApiLogService.save(rsalEntity);
                    rsalUno = rsalEntity.getRslUno();
                    int finalRsalUno = rsalUno;
                    CompletableFuture
                            .supplyAsync(() -> {
                                try {
                                    // text기반 검색 및 결과 저장.(이미지)
                                    return search(url, nationCode,  CustomResult.class, CustomResult::getError, CustomResult::getResults, finalRsalUno);
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
                                            , CustomResults::getImage
                                            , CustomResults::getTitle
                                            , CustomResults::getLink
                                            , CustomResults::isFacebook
                                            , CustomResults::isInstagram
                                            , CustomResults::isTwitter
                                            , nationCode
                                            , "google_lens"
                                    );
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // 검색을 통해 결과 db에 적재.
                                    if (r == null) {
                                        return null;
                                    }
                                    return saveImgSearch(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public <INFO, RESULT> List<RESULT> search(String url, String nationCode, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn, int rsalUno) throws Exception {
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
    public <RESULT> List<SearchResultEntity> save(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn, String nationCode, String engine) throws Exception {

        if (results == null) {
            log.info("result null");
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
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultEntity2(insertResult.getTsiUno(), tsrSns, result, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

//                int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
//                if (cnt > 0) {
//                    log.info("file cnt === {}", cnt);
//                } else {
                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplateConfig.customRestTemplate(), sre, result, getThumbnailFn, false);
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

    public List<String> processVideo(SearchInfoEntity insertResult) throws Exception {
        List<String> files = new ArrayList<>();

        log.debug("Python Call");
        String[] command = new String[4];
        //python C:/utils/extract_keyframes.py C:/utils/input_Vid.mp4 C:/data/requests/20230312
        command[0] = "python";
        command[1] = sitProperties.getPythonVideoModule();
        command[2] = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        command[3] = insertResult.getTsiImgPath() + insertResult.getTsiUno();
        try {
            execPython(command);

            String DATA_DIRECTORY = insertResult.getTsiImgPath() + insertResult.getTsiUno() + "/";
            File dir = new File(DATA_DIRECTORY);

            String[] filenames = dir.list();
            for (String filename : filenames) {
                files.add(insertResult.getTsiImgPath() + insertResult.getTsiUno() + "/" + filename);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        return files;
    }

    public String saveImgSearch(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
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

    public VideoInfoEntity saveVideoInfo(VideoInfoEntity vie) {
        vie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        vie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        return videoInfoRepository.save(vie);
    }

    public void saveErrorInfo(SearchInfoEntity param){
        param.setTsiStat("99");
        if(param != null) searchInfoRepository.save(param);
    }
}
