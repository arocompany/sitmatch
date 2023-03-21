package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.common.Consts;
import com.nex.search.entity.*;
import com.nex.search.repo.*;
import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.nex.common.CmnUtil.execPython;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final EntityManager em;

    @Data
    static class YandexByTextResult {
        private String error;
        private List<Images_resultsByText> images_results;
    }

    @Data
    static class Images_resultsByText {
        private int position;
        private String original;
        private int original_width;
        private int original_height;
        private String source;
        private String title;
        private String link;
        private String thumbnail;

        public boolean isInstagram() {
            return "Instagram".equals(source);
        }

        public boolean isFacebook() {
            return "Facebook".equals(source);
        }
    }

    @Data
    static class YandexByImageResult {
        private String error;
        private List<Images_resultsByImage> inline_images;
    }

    @Data
    static class Images_resultsByImage {
        private String original;
        private String source;
        private String title;
        private String link;
        private String thumbnail;
        private String source_name;

        public boolean isInstagram() {
            return "Instagram".equals(source);
        }

        public boolean isFacebook() {
            return "Facebook".equals(source);
        }
    }

    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SearchJobRepository searchJobRepository;
    private final MatchResultRepository matchResultRepository;

    @Autowired
    ResourceLoader resourceLoader;

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
    @Value("${search.yandex.text.api_key}")
    private String textYandexApikey;
    @Value("${search.yandex.image.engine}")
    private String imageYandexEngine;
    @Value("${server.url}")
    private String serverIp;

    public List<SearchResultEntity> searchYandexByText(String url, String tsrSns, SearchInfoEntity insertResult) throws Exception {
        String jsonInString = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<SearchResultEntity> sreList = new ArrayList<SearchResultEntity>();
        if(resultMap.getStatusCodeValue() == 200){

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            jsonInString = mapper.writeValueAsString(resultMap.getBody());
            YandexByTextResult yandexByTextResult = mapper.readValue(jsonInString, YandexByTextResult.class);
            if(yandexByTextResult.getError() == null){

                SearchResultEntity sre = null;
                for(Images_resultsByText images_result : yandexByTextResult.getImages_results()){
                    try {
                        log.debug(" ### images_result.link ### : {} ", images_result.getLink());
                        String imageUrl = images_result.getOriginal();
                        sre = new SearchResultEntity();
                        sre.setTsiUno(insertResult.getTsiUno());
                        sre.setTsrJson(images_result.toString());
                        sre.setTsrDownloadUrl(imageUrl);
                        sre.setTsrTitle(images_result.getTitle());
                        sre.setTsrSiteUrl(images_result.getLink());
                        //sre.setTsrSns("11");

                        //2023-03-20
                        //Facebook, Instagram 도 Google 로 검색, source 값으로 Facebook, Instagram 판별

                        //Facebook 검색이고, source 값이 Facebook 인 경우
                        if ("17".equals(tsrSns) && images_result.isFacebook()) {
                            sre.setTsrSns("17");
                        }
                        //Instagram 검색이고, source 값이 Instagram 인 경우
                        else if ("15".equals(tsrSns) && images_result.isInstagram()) {
                            sre.setTsrSns("15");
                        }
                        //그 외는 구글
                        else {
                            sre.setTsrSns("11");
                        }

                        
                        //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                        if (!tsrSns.equals(sre.getTsrSns())) {
                            continue;
                        }


                        //Resource resource = resourceLoader.getResource(imageUrl);
                        //2023-03-21
                        //구글은 original, Facebook, Instagram 는 thumbnail 로 값을 가져오도록 변경
                        Resource resource = resourceLoader.getResource("11".equals(sre.getTsrSns()) ? imageUrl : images_result.getThumbnail());


                        if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("")) {

                            LocalDate now = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                            String folder = now.format(formatter);
                            String restrictChars = "|\\\\?*<\":>/";
                            String regExpr = "[" + restrictChars + "]+";
                            String uuid = UUID.randomUUID().toString();
                            String extension = "";
                            String extension_ = "";
                            if(resource.getFilename().indexOf(".") > 0){
                                extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                                extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                                extension_ = extension.substring(1);
                            }


                            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                            File destdir = new File(fileLocation2 + folder + File.separator + insertResult.getTsiUno());
                            if (!destdir.exists()) {
                                destdir.mkdirs();
                            }

                            Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                            sre.setTsrImgExt(extension_);
                            sre.setTsrImgName(uuid + extension);
                            sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                            Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                            sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                            sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                            sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                            img.flush();
                        }
                        log.debug("url ::::: "+url+"    +++++++++++++++++++++++      sre ::::: "+sre);

                        saveSearchResult(sre);
                        sreList.add(sre);
                    } catch(IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                        log.error(e.getMessage());
                        e.printStackTrace();
                        throw new IOException(e);
                    } catch(Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                }
            }
        }

        return sreList;
    }

    public String saveImgSearchYandexByText(List<SearchResultEntity> result, SearchInfoEntity insertResult) throws Exception {

        insertResult.setTsiStat("13");
        if(insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()){
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\","/"));
        }
        SearchInfoEntity updateResult = saveSearchInfo(insertResult);

        List<SearchResultEntity> searchResultEntity = result;
        SearchJobEntity sje = null;
        for (SearchResultEntity sre : searchResultEntity){
            try {
                sje = new SearchJobEntity();
                sje.setTsiUno(sre.getTsiUno());
                sje.setTsrUno(sre.getTsrUno());
                if(insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
                    sje.setTsrImgPath(sre.getTsrImgPath().replaceAll("\\\\", "/"));
                } else {
                    sje.setTsrImgPath("");
                }
                sje.setTsrImgName(sre.getTsrImgName());
                sje.setTsrImgExt(sre.getTsrImgExt());
                saveSearchJob(sje);
            } catch(JpaSystemException e){
                log.error(e.getMessage());
                e.printStackTrace();
                throw new JpaSystemException(e);
            } catch(Exception e){
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }

        return "저장 완료";
    }

    public List<SearchResultEntity> searchYandexByImage(String url, String tsrSns, SearchInfoEntity insertResult) throws Exception {
        String jsonInString = "";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<SearchResultEntity> sreList = new ArrayList<SearchResultEntity>();
        if(resultMap.getStatusCodeValue() == 200){

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            jsonInString = mapper.writeValueAsString(resultMap.getBody());
            YandexByImageResult YandexByImageResult = mapper.readValue(jsonInString, YandexByImageResult.class);
            if(YandexByImageResult.getError() == null){

                SearchResultEntity sre = null;
                for(Images_resultsByImage images_result : YandexByImageResult.getInline_images()){
                    try {

                        String imageUrl = images_result.getOriginal();
                        sre = new SearchResultEntity();
                        sre.setTsiUno(insertResult.getTsiUno());
                        sre.setTsrJson(images_result.toString());
                        sre.setTsrDownloadUrl(imageUrl);
                        sre.setTsrTitle(images_result.getTitle());
                        sre.setTsrSiteUrl(images_result.getLink());
                        //sre.setTsrSns("11");

                        //2023-03-20
                        //Facebook, Instagram 도 Google 로 검색, source 값으로 Facebook, Instagram 판별

                        //Facebook 검색이고, source 값이 Facebook 인 경우
                        if ("17".equals(tsrSns) && images_result.isFacebook()) {
                            sre.setTsrSns("17");
                        }
                        //Instagram 검색이고, source 값이 Instagram 인 경우
                        else if ("15".equals(tsrSns) && images_result.isInstagram()) {
                            sre.setTsrSns("15");
                        }
                        //그 외는 구글
                        else {
                            sre.setTsrSns("11");
                        }


                        //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                        if (!tsrSns.equals(sre.getTsrSns())) {
                            continue;
                        }


                        //Resource resource = resourceLoader.getResource(imageUrl);
                        //2023-03-21
                        //구글은 original, Facebook, Instagram 는 thumbnail 로 값을 가져오도록 변경
                        Resource resource = resourceLoader.getResource("11".equals(sre.getTsrSns()) ? imageUrl : images_result.getThumbnail());


                        if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("")) {

                            LocalDate now = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                            String folder = now.format(formatter);
                            String restrictChars = "|\\\\?*<\":>/";
                            String regExpr = "[" + restrictChars + "]+";
                            String uuid = UUID.randomUUID().toString();
                            String extension = "";
                            String extension_ = "";
                            if(resource.getFilename().indexOf(".") > 0){
                                extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                                extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                                extension_ = extension.substring(1);
                            }


                            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                            File destdir = new File(fileLocation2 + folder + File.separator + insertResult.getTsiUno());
                            if (!destdir.exists()) {
                                destdir.mkdirs();
                            }

                            Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                            sre.setTsrImgExt(extension_);
                            sre.setTsrImgName(uuid + extension);
                            sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                            Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                            sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                            sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                            sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                            img.flush();

                        }
                        saveSearchResult(sre);
                        sreList.add(sre);
                    } catch(IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                        log.error(e.getMessage());
                        e.printStackTrace();
                        throw new IOException(e);
                    } catch(Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                }
            }
        }

        return sreList;
    }

    public String saveImgSearchYandexByImage(List<SearchResultEntity> result, SearchInfoEntity insertResult) throws Exception {
        CompletableFuture.allOf().join();

        insertResult.setTsiStat("13");
        if(insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()){
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\","/"));
        }
        SearchInfoEntity updateResult = saveSearchInfo(insertResult);

        List<SearchResultEntity> searchResultEntity = result;
        SearchJobEntity sje = null;
        for (SearchResultEntity sre : searchResultEntity){
            try {
                sje = new SearchJobEntity();
                sje.setTsiUno(sre.getTsiUno());
                sje.setTsrUno(sre.getTsrUno());
                if(insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
                    sje.setTsrImgPath(sre.getTsrImgPath().replaceAll("\\\\", "/"));
                } else {
                    sje.setTsrImgPath("");
                }
                sje.setTsrImgName(sre.getTsrImgName());
                sje.setTsrImgExt(sre.getTsrImgExt());
                saveSearchJob(sje);
            } catch(JpaSystemException e){
                log.error(e.getMessage());
                e.printStackTrace();
                throw new JpaSystemException(e);
            } catch(Exception e){
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }

        return "저장 완료";
    }

    public List<String> processVideo(SearchInfoEntity insertResult) throws Exception{
        List<String> files = new ArrayList<String>();
        log.debug("Python Call");
        String[] command = new String[4];
        //python C:/utils/extract_keyframes.py C:/utils/input_Vid.mp4 C:/data/requests/20230312
        command[0] = "python";
        command[1] = pythonVideoModule;
        command[2] = insertResult.getTsiImgPath()+insertResult.getTsiImgName();
        command[3] = insertResult.getTsiImgPath()+insertResult.getTsiUno();
        try {
            execPython(command);

            String DATA_DIRECTORY = insertResult.getTsiImgPath()+insertResult.getTsiUno()+"/";
            File dir = new File(DATA_DIRECTORY);

            String[] filenames = dir.list();
            for (String filename : filenames) {
                files.add(insertResult.getTsiImgPath()+insertResult.getTsiUno()+"/"+filename);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        return files;
    }

    // Yandex 영상 검색 후처리
    @Async
    public void searchYandexByVideo(String tsrSns, SearchInfoEntity insertResult, String folder, String location3){
        try {
            List<String> files = processVideo(insertResult);
            for (int i = 0; i < files.size(); i++){
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/")+1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0,files.get(i).lastIndexOf("/")+1));
                saveVideoInfo(videoInfo);

                String url = textYandexUrl
                        + "?GL=" + textYandexGl
                        + "&no_cache=" + textYandexNocache
                        + "&api_key=" + textYandexApikey
                        + "&engine=" + imageYandexEngine
                        + "&image_url=" + serverIp+folder+"/"+location3+"/"+insertResult.getTsiUno()+files.get(i).substring(files.get(i).lastIndexOf("/"));

                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                // text기반 yandex 검색 및 결과 저장.(이미지)
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.debug(e.getMessage());
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandexByImage(r, insertResult);
                            } catch (Exception e) {
                                log.debug(e.getMessage());
                                return null;
                            }
                        });
            }
        } catch (Exception e){
            log.debug(e.getMessage());
        }
    }

    public SearchJobEntity saveSearchJob(SearchJobEntity sje){
        sje.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setTsjStatus("00");
        return searchJobRepository.save(sje);
    }

    public SearchInfoEntity saveSearchInfo(SearchInfoEntity sie){
        sie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setDataStatCd("10");
        return searchInfoRepository.save(sie);
    }

    public VideoInfoEntity saveVideoInfo(VideoInfoEntity vie){
        vie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        vie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        return videoInfoRepository.save(vie);
    }

    public SearchResultEntity saveSearchResult(SearchResultEntity sre){
        sre.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setDataStatCd("10");
        return searchResultRepository.save(sre);
    }

    public Page<DefaultQueryDtoInterface> getSearchResultList(Integer tsiUno, String keyword, Integer page, String priority,
                                                              String tsjStatusAll, String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4) {
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        if(tsjStatusAll.equals("1")) {
            tsjStatus1 = "00";
            tsjStatus2 = "01";
            tsjStatus3 = "10";
            tsjStatus4 = "11";
        }
        log.debug("priority => {}", priority);

        String orderByTmrSimilarityDesc = " ORDER BY tmrSimilarity desc, TMR.TSR_UNO desc";

        if(priority.equals("1")) {
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4, pageRequest);
        } else {
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityAsc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4, pageRequest);
        }
    }

    public DefaultQueryDtoInterface getResultInfo(Integer tsrUno) {
        return searchResultRepository.getResultInfo(tsrUno);
    }

    public Page<DefaultQueryDtoInterface> getTraceList(Integer page, String trkStatCd, String keyword) {
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        if(trkStatCd.equals("삭제 요청 중")) {
            trkStatCd = "20";
        } else if(trkStatCd.equals("관리 중")) {
            trkStatCd = "10";
        } else {
            trkStatCd = "";
        }
        return searchResultRepository.getTraceList(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, trkStatCd, keyword, pageRequest);
    }

    public List<DefaultQueryDtoInterface> getTraceListByHome() {
        return searchResultRepository.getTraceListByHome(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, "", "");
    }

    public DefaultQueryDtoInterface getTraceInfo(Integer tsrUno) {
        return searchResultRepository.getTraceInfo(tsrUno);
    }

    public List<VideoInfoEntity> getVideoInfoList(Integer tsiUno) {
        return videoInfoRepository.findAllByTsiUno(tsiUno);
    }


    public Map<String, Object> getTraceHistoryList(Integer page, String keyword) {
        Map<String,Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료

        return outMap;
    }

    public Map<Integer, String> getTsiTypeMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiTypeMap = new HashMap<>();

        for(SearchInfoEntity list : searchInfoEntityList) {
            tsiTypeMap.put(list.getTsiUno(), list.getTsiType());
        }

        return tsiTypeMap;
    }

    public Map<Integer, String> getTsiKeywordMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiKeywordMap = new HashMap<>();

        for(SearchInfoEntity list : searchInfoEntityList) {
            tsiKeywordMap.put(list.getTsiUno(), list.getTsiKeyword());
        }

        return tsiKeywordMap;
    }

    public Map<Integer, Timestamp> getTsiFstDmlDtMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, Timestamp> tsiFstDmlDtMap = new HashMap<>();

        for(SearchInfoEntity list : searchInfoEntityList) {
            tsiFstDmlDtMap.put(list.getTsiUno(), list.getFstDmlDt());
        }

        return tsiFstDmlDtMap;
    }

    public String getSearchInfoImgUrl(Integer tsiUno) {
        return searchInfoRepository.getSearchInfoImgUrl(tsiUno);
    }

    public String getSearchInfoTsiType(Integer tsiUno) {
        return searchInfoRepository.getSearchInfoTsiType(tsiUno);
    }

    public void addTrkStat(Integer tsrUno) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setTrkStatCd(StringUtils.hasText(searchResultEntity.getTrkStatCd()) ? Consts.TRK_STAT_CD_NULL : Consts.TRK_STAT_CD_MONITORING);
        searchResultRepository.save(searchResultEntity);
    }

    public void deleteSearchInfo(Integer tsiUno) {
        SearchInfoEntity searchInfoEntity = searchInfoRepository.findByTsiUno(tsiUno);
        searchInfoEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
        searchInfoRepository.save(searchInfoEntity);
    }

    public void deleteMornitoringInfo(Integer tsrUno) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
        searchResultRepository.save(searchResultEntity);
    }

    public void setTrkHistMemo(Integer tsrUno, String memo) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setTrkHistMemo(memo);
        searchResultRepository.save(searchResultEntity);
    }

    public void setTrkStatCd(Integer tsrUno, String trkStatCd) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setTrkStatCd(trkStatCd);
        searchResultRepository.save(searchResultEntity);
    }

    public Map<String, Object> getSearchInfoList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingOrderByTsiUnoDesc("10", keyword, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndUserUnoOrderByTsiUnoDesc("10", keyword, userUno, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    public Map<Integer, String> getUserIdMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByUserUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for(UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getUserUno(), item.getUserId());
        }

        return userIdMap;
    }
    public Map<Integer, String> getUserIdByTsiUnoMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByTsiUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for(UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getTsiUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getProgressPercentMap() {
        List<SearchJobRepository.ProgressPercentDtoInterface> progressPercentList = searchJobRepository.progressPercentByAll();
        Map<Integer, String> progressPercentMap = new HashMap<>();

        // progressPercentList => progressPercentMap
        for(SearchJobRepository.ProgressPercentDtoInterface item : progressPercentList) {
            progressPercentMap.put(item.getTsiUno(), String.format("%d%%", item.getProgressPercent()));
        }

        return progressPercentMap;
    }

}

