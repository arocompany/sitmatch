package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.CommonCode;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.RestTemplateConfig;
import com.nex.common.SitProperties;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.VideoInfoEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.entity.result.Images_resultsByImage;
import com.nex.search.entity.result.YandexByImageResult;
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
import org.springframework.util.StringUtils;
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
public class SearchVideoService {
    private final ImageService imageService;

    private final RestTemplateConfig restTemplateConfig;
    private final SearchInfoRepository searchInfoRepository;
    private final SearchJobRepository searchJobRepository;
    private final SearchResultRepository searchResultRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SitProperties sitProperties;

    @Async
    public void searchYandexByTextVideo(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String path, String nationCode) throws Exception {
        List<String> files = processVideo(insertResult);

        for(int i=0; i<files.size(); i++) {
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setTsiUno(insertResult.getTsiUno());
            videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
            videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
            saveVideoInfo(videoInfo);
        }
        String tsiKeywordHiddenValue = null;
        if(StringUtils.hasText(searchInfoDto.getTsiKeywordHiddenValue())) {
            tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

            if (CommonCode.snsTypeInstagram.equals(tsrSns)) { tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue; }
            else if (CommonCode.snsTypeFacebook.equals(tsrSns)) { tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue; }
        }

        try {
            for (int i = 0; i < files.size(); i++) {
                String searchImageUrl = sitProperties.getServerIp() + sitProperties.getFileLocation3() + "/" + path + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                // searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                try {
//                    String url = textYandexUrl
//                            + "?gl=" + textYandexGl
//                            + "&q=" + tsiKeywordHiddenValue
//                            + "&no_cache=" + textYandexNocache
//                            + "&api_key=" + textYandexApikey
//                            + "&engine=" + imageYandexEngine
//                            + "&safe=off"
//                            + "&filter=0"
//                            + "&nfpr=0"
//                            + "&image_url=" + searchImageUrl;

                    String url = CommonStaticSearchUtil.getSerpApiUrlForGoogle(sitProperties.getTextYandexUrl(), tsiKeywordHiddenValue, nationCode, sitProperties.getTextYandexNocache(), sitProperties.getTextYandexLocation(), null, sitProperties.getTextYandexApikey(), searchImageUrl, sitProperties.getImageYandexEngine());

                    CompletableFuture
                            .supplyAsync(() -> {
                                try {
                                    // text기반 yandex 검색 및 결과 저장.(이미지)
                                    return searchYandex(url, YandexByImageResult.class, YandexByImageResult::getError, YandexByImageResult::getInline_images);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            }).thenApply((r) -> {
                                try {
                                    log.info("R" + r);
                                    //검색 결과를 SearchResult Table에 저장 및 이미지 저장
                                    return saveYandex(
                                            r
                                            , tsrSns
                                            , insertResult
                                            , Images_resultsByImage::getOriginal
                                            , Images_resultsByImage::getThumbnail
                                            , Images_resultsByImage::getTitle
                                            , Images_resultsByImage::getLink
                                            , Images_resultsByImage::isFacebook
                                            , Images_resultsByImage::isInstagram
                                    );
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    if(r == null){ return null; }
                                    return saveImgSearchYandex(r, insertResult);
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

    public <INFO, RESULT> List<RESULT> searchYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        try {
            log.info("searchYandex 진입");
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            ResponseEntity<?> resultMap = restTemplateConfig.customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

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

            log.debug("results: " + results);
            return results != null ? results : new ArrayList<>();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return null;
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
                String imageUrl = getOriginalFn.apply(result);
                log.info("imageUrl1: "+imageUrl);
                if(imageUrl == null) {
                    imageUrl = getThumbnailFn.apply(result);
                }
                log.info("imageUrl2: "+imageUrl);
                if(imageUrl != null) {
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = CommonStaticSearchUtil.getSearchResultEntity2(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

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
        // List<String> files = new ArrayList<String>();
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

    public String saveImgSearchYandex(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
        // SearchInfoEntity updateResult = saveSearchInfo_2(insertResult);
//        saveSearchInfo_2(insertResult);
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