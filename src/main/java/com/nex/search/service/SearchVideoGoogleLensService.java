package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.*;
import com.nex.requestSerpApiLog.RequestSerpApiLogService;
import com.nex.search.entity.*;
import com.nex.search.entity.result.GoogleLensImagesByImageResult;
import com.nex.search.entity.result.Images_resultsByGoogleLens;
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
import java.util.List;
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
    public void searchByGoogleLensVideo(String tsrSns, SearchInfoEntity insertResult, String path, String nationCode) throws Exception {
        List<String> files = processVideo(insertResult);

        for (int i = 0; i < files.size(); i++) {
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setTsiUno(insertResult.getTsiUno());
            videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
            videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
            saveVideoInfo(videoInfo);
        }

        try {
            ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

            for (int i = 0; i < files.size(); i++) {
                String searchImageUrl = configData.getHostImageUrl() + sitProperties.getFileLocation3() + "/" + path + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));

                int rsalUno = 0;
                try {
                    String url = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), null, nationCode, null, null, null, configData.getSerpApiKey(), searchImageUrl, "google_lens", null);
                    RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.init(insertResult.getTsiUno(), url, nationCode, "google_lens", null, null, configData.getSerpApiKey(), searchImageUrl);
                    requestSerpApiLogService.save(rsalEntity);
                    rsalUno = rsalEntity.getRslUno();
                    int finalRsalUno = rsalUno;
                    CompletableFuture
                            .supplyAsync(() -> {
                                try {
                                    // text기반 검색 및 결과 저장.(이미지)
                                    return search(url, nationCode,  GoogleLensImagesByImageResult.class, GoogleLensImagesByImageResult::getError, GoogleLensImagesByImageResult::getImage_sources, finalRsalUno);
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
                                            , Images_resultsByGoogleLens::getOriginal
                                            , Images_resultsByGoogleLens::getThumbnail
                                            , Images_resultsByGoogleLens::getTitle
                                            , Images_resultsByGoogleLens::getLink
                                            , Images_resultsByGoogleLens::isFacebook
                                            , Images_resultsByGoogleLens::isInstagram
                                            , Images_resultsByGoogleLens::isTwitter
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
            ResponseEntity<?> resultMap = new RestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody());
            JsonNode rootNode = mapper.readTree(jsonInString);
            String pageToken = rootNode.at("/image_sources_search/page_token").asText();

            // pageToken값 출력
            log.info("google_lens page_token === {} ", pageToken);

            String sourcesUrl = CommonStaticSearchUtil.getSerpApiUrl(sitProperties.getTextUrl(), null, nationCode, null, null, null, configData.getSerpApiKey(), null, "google_lens_image_sources", pageToken);

            HttpHeaders sourcesHeader = new HttpHeaders();
            HttpEntity<?> sourcesEntity = new HttpEntity<>(sourcesHeader);
            UriComponents sourcesUri = UriComponentsBuilder.fromHttpUrl(sourcesUrl).build();
            ResponseEntity<?> sourcesResultMap = new RestTemplate().exchange(sourcesUri.toString(), HttpMethod.GET, sourcesEntity, Object.class);

            List<RESULT> results = null;

            RequestSerpApiLogEntity rsalEntity = requestSerpApiLogService.select(rsalUno);

            if (sourcesResultMap.getStatusCodeValue() == 200) {
                ObjectMapper sourcesMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String sourcesJsonInString = sourcesMapper.writeValueAsString(sourcesResultMap.getBody());
                INFO info = sourcesMapper.readValue(sourcesJsonInString, infoClass);

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
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) throws Exception {

        if (results == null) {
            log.info("result null");
            return null;
        }

        List<SearchResultEntity> sreList = new ArrayList<>();
        for (RESULT result : results) {
            try {
                //검색 결과 엔티티 추출
                SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultEntity2(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn, isTwitterFn);

                //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                if (!tsrSns.equals(sre.getTsrSns())) {
                    continue;
                }

                int cnt = searchResultRepository.countByTsrSiteUrl(sre.getTsrSiteUrl());
                if (cnt > 0) {
                    log.info("file cnt === {}", cnt);
                } else {
                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), restTemplateConfig.customRestTemplate(), sre, result, getOriginalFn, getThumbnailFn, false);
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
}
