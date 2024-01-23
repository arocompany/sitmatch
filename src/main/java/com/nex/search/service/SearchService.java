package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.Chart.entity.*;
import com.nex.Chart.repo.*;
import com.nex.common.*;

import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.repository.NationCodeRepository;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.VideoInfoEntity;
import com.nex.search.entity.dto.*;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultRepository;
import com.nex.search.repo.VideoInfoRepository;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Configuration
@RequiredArgsConstructor
public class SearchService {
    private final RestTemplateConfig restTemplateConfig;

    private final SearchImageService searchImageService;
    private final SearchImageGoogleLensService searchImageGoogleLensService;
    private final SearchTextService searchTextService;
    private final SearchTextImageService searchTextImageService;
    private final SearchVideoService searchVideoService;
    private final SearchYoutubeService searchYoutubeService;

    private final NationCodeRepository nationCodeRepository;

    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SearchJobRepository searchJobRepository;
    private final MonitoringHistRepository monitoringHistRepository;
    private final DeleteReqHistRepository deleteReqHistRepository;
    private final DeleteComptHistRepository deleteComptHistRepository;
    private final AlltimeMonitoringHistRepository alltimeMonitoringHistRepository;

    private final SearchInfoHistRepository searchInfoHistRepository;
    private final TraceHistRepository traceHistRepository;
    private final SearchResultHistRepository searchResultHistRepository;
    private final NoticeHistRepository noticeHistRepository;

    private final SitProperties sitProperties;

    public SearchInfoEntity insertSearchInfo(MultipartFile uploadFile, SearchInfoEntity param, String folder){
        boolean isFile = ! uploadFile.isEmpty();

        if(isFile){ // 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            try{
                InputStream inputStream = uploadFile.getInputStream();
                Tika tika = new Tika();
                String mimeType = tika.detect(inputStream);
                if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("video")){// 비디오
                    param.setTsiImgHeight("");
                    param.setTsiImgWidth("");
                    param.setTsiImgSize(String.valueOf(uploadFile.getSize() / 1024));
                    param.setSearchValue("0");
                    if(! StringUtils.hasText(param.getTsiKeyword())){
                        param.setTsiType("19");
                    } else {
                        param.setTsiType("15");
                    }
                } else if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("image")){// 이미지 업로드
                    BufferedImage bi = ImageIO.read(uploadFile.getInputStream());

                    // TODO : image/webp mime type에 대한 후 처리 또는 예외 처리 필요
                    if(bi != null) {
                        param.setTsiImgHeight(String.valueOf(bi.getHeight()));
                        param.setTsiImgWidth(String.valueOf(bi.getWidth()));
                        param.setTsiImgSize(String.valueOf(uploadFile.getSize() / 1024));
                        bi.flush();
                    }

                    if(! StringUtils.hasText(param.getTsiKeyword())){
                        param.setTsiType("17");
                    } else {
                        param.setTsiType("13");
                    }
                }

                String origName = uploadFile.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String extension = origName.substring(origName.lastIndexOf("."));

                String filePath = sitProperties.getFileLocation1()+folder;
                File destDir = new File(filePath);
                if(!destDir.exists()){
                    destDir.mkdirs();
                }

                uploadFile.transferTo(new File(destDir+File.separator+uuid+extension));

                param.setTsiImgName(uuid+extension);
                param.setTsiImgPath((destDir+File.separator).replaceAll("\\\\", "/"));
                param.setTsiImgExt(extension.substring(1));
            }catch(Exception e){
                e.printStackTrace();
                log.error(e.getMessage());
                return null;
            }
        } else {
            param.setTsiType("11");
        }

        return saveSearchInfo(param);
    }
    public void search(SearchInfoEntity param, SearchInfoDto siDto, String folder){
        try {
            List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
            for (NationCodeEntity ncInfo : ncList) {
                // 검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19:영상
                switch (param.getTsiType()) {
                    case CommonCode.searchTypeKeyword -> { // 11:키워드
                        searchYoutubeService.searchYandexYoutube(CommonCode.snsTypeGoogle, param, siDto, ncInfo.getNcCode().toLowerCase());
                        if (param.getTsiGoogle() == 1) {
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle);
                        }
                        if (param.getTsiInstagram() == 1) {
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram);
                        }
                        if (param.getTsiFacebook() == 1) {
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook);
                        }
                    }
                    case CommonCode.searchTypeKeywordImage -> { // 13:키워드+이미지
                        searchImageGoogleLensService.searchYandexByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase());
                        if (param.getTsiGoogle() == 1) {
                            searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle);
                        }
                        if (param.getTsiInstagram() == 1) {
                            searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram);
                        }
                        if (param.getTsiFacebook() == 1) {
                            searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook);
                        }
                    }
                    case CommonCode.searchTypeKeywordVideo -> {
                        // 15:키워드+영상
//                            search(param, folder, siDto);
                        if (param.getTsiGoogle() == 1) { searchVideoService.searchYandexByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase());}
                        if (param.getTsiFacebook() == 1) { searchVideoService.searchYandexByTextVideo(CommonCode.snsTypeFacebook, param, siDto, folder, ncInfo.getNcCode().toLowerCase()); }
                        if (param.getTsiInstagram() == 1) { searchVideoService.searchYandexByTextVideo(CommonCode.snsTypeInstagram, param, siDto, folder, ncInfo.getNcCode().toLowerCase()); }
                    }
                    case CommonCode.searchTypeImage -> { // 17:이미지
                        searchImageGoogleLensService.searchYandexByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase());
                        searchImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase());
                    }
                    case CommonCode.searchTypeVideo -> {// 19: 영상
                        searchVideoService.searchYandexByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase());
                    }
                }
                // searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder, searchInfoDto);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

//    /**
//     * 텍스트, 이미지 검색
//     *
//     * {@link #searchYandexByText(String, String, SearchInfoEntity)} {@link #searchYandexByImage(String, String, SearchInfoEntity)}}
//     *
//     * @param url         (URL)
//     * @param infoClass   (YandexByTextResult or YandexByImageResult Class)
//     * @param getErrorFn  (info error getter Function)
//     * @param getResultFn (RESULT getter Function)
//     * @param <INFO>      (YandexByTextResult or YandexByImageResult)
//     * @param <RESULT>    (Images_resultsByText or Images_resultsByImage)
//     * @return List<RESULT> (RESULT List)
//     * @throws Exception
//     */

    // 배치시 진입
    public <INFO, RESULT> List<RESULT> searchBatchYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        try {
            log.info("searchBatchYandex 진입");
            HttpHeaders header = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(header);
            UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
            // ResponseEntity<?> resultMap = new customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
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
        }catch(Exception e){
            log.error(e.getMessage());
        }
        return null;
    }

    public SearchInfoEntity saveSearchInfo(SearchInfoEntity sie) {
        CommonStaticSearchUtil.setSearchInfoDefault(sie);
        return searchInfoRepository.save(sie);
    }

//    /**
//     * 검색 결과 엔티티 기본값 세팅
//     *
//     * @param sre (검색 결과 엔티티)
//     */
    public Page<DefaultQueryDtoInterface> getSearchResultList(Integer tsiUno, String keyword, Integer page, String priority,
                                                              String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                              String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String order_type) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        log.debug("priority => {}", priority);

        // String orderByTmrSimilarityDesc = " ORDER BY tmrSimilarity desc, TMR.TSR_UNO desc";

        if ("1".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_1");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_1(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("2".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_2");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_2(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("3".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_3");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_3(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("4".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_4");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_4(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("5".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_5");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_5(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("6".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_6");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_6(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else {
            log.info("getResultInfoListOrderByTmrSimilarityDesc");
            log.info("pageRequest" + pageRequest);

            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        }
    }

    // Integer percent,
    public Page<DefaultQueryDtoInterface> getNoticeList(Integer page, Integer tsiUno, String tsiKeyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        log.info("pageRequest: " + pageRequest);
        log.info("tsiuno: " + tsiUno);

        if (tsiUno == 0) {
            // return searchResultRepository.getNoticeList(pageRequest, percent);
            return searchResultRepository.getNoticeList(pageRequest);
        } else {
            return StringUtils.hasText(tsiKeyword) ? searchResultRepository.getNoticeSelList(pageRequest, tsiUno, tsiKeyword) : searchResultRepository.getNoticeSelListEmptyKeyword(pageRequest, tsiUno);
            // return searchResultRepository.getNoticeSelList(pageRequest, tsiUno, percent, tsiKeyword);
        }
    }

    public List<DefaultQueryDtoInterface> getNoticeListMain(Integer percent) {
        return searchResultRepository.getNoticeListMain(percent);
    }

    public DefaultQueryDtoInterface getInfoList(Integer tsiUno) {
        return searchResultRepository.getInfoList(tsiUno);
    }

    public Page<DefaultQueryDtoInterface> getTraceList(Integer page, String trkStatCd, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        if (trkStatCd.equals("삭제 요청 중")) {
            trkStatCd = "20";
        } else if (trkStatCd.equals("관리 중")) {
            trkStatCd = "10";
        } else {
            trkStatCd = "";
        }
        return searchResultRepository.getTraceList(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, trkStatCd, keyword, pageRequest);
    }

    public DefaultQueryDtoInterface getTraceInfo(Integer tsrUno) {
        return searchResultRepository.getTraceInfo(tsrUno);
    }

    public List<VideoInfoEntity> getVideoInfoList(Integer tsiUno) {
        return videoInfoRepository.findAllByTsiUno(tsiUno);
    }

    public Map<String, Object> getTraceHistoryList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryList(keyword, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryUserFileList(Integer page, String keyword) {
        log.info(" == getTraceHistoryUserFileList 진입 == ");
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceUserFileList(keyword, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }
    public Map<String, Object> getTraceHistoryMonitoringList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringList(keyword, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoList(keyword, pageRequest, tsiUno);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoList(keyword, pageRequest, tsiUno);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }
    
    // 대상자는 없고 대상자키워드 있을때
    public Map<String, Object> getTraceHistoryMonitoringUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringUserFileList(keyword, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqList(keyword, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqUserFileList(keyword, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoUserFileList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptList(keyword, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptUserFileList(keyword, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoUserFileList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoUserFileList(keyword, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }


    public Map<String, Object> allTimeMonitoringList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringList(keyword, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링


        return outMap;
    }

    public Map<String, Object> allTimeMonitoringUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringUserFileList(keyword, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링


        return outMap;
    }

    public Map<Integer, String> getTsiTypeMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiTypeMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
            tsiTypeMap.put(list.getTsiUno(), list.getTsiType());
        }

        return tsiTypeMap;
    }

    public Map<Integer, String> getTsiKeywordMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiKeywordMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
            tsiKeywordMap.put(list.getTsiUno(), list.getTsiKeyword());
        }

        return tsiKeywordMap;
    }

    public Map<Integer, Timestamp> getTsiFstDmlDtMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, Timestamp> tsiFstDmlDtMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
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

    public void addTrkStat(int userUno,String userId, Integer tsrUno) {
        String trkStatCd = searchResultRepository.getTrkStatCd(tsrUno);
        if ("10".equals(trkStatCd)) {
            searchResultRepository.subTrkStat(tsrUno);
        } else {
            MonitoringHistEntity mtr = new MonitoringHistEntity();
            mtr.setTmhTsrUno(tsrUno);
            mtr.setUserId(userId);
            mtr.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            mtr.setUserUno(userUno);
            monitoringHistRepository.save(mtr);
            searchResultRepository.addTrkStat(tsrUno);
        }
    }

    public void deleteTsiUnos(List<Integer> tsiUnosValue) {
        List<SearchInfoEntity> searchInfoEntity = searchInfoRepository.findByTsiUnoIn(tsiUnosValue);
        for (SearchInfoEntity infoEntity : searchInfoEntity) {
            infoEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
            searchInfoRepository.save(infoEntity);
        }
    }

    public void deleteTsrUnos(List<Integer> tsrUnoValues) {
        log.info("추적이력 일괄삭제 진입");
        List<SearchResultEntity> sre = searchResultRepository.findByTsrUnoIn(tsrUnoValues);
        // List<MatchResultEntity> mre = matchResultRepository.findByTsrUnoIn(tsrUnoValues);
        for (SearchResultEntity searchResultEntity : sre) {
            searchResultEntity.setTrkStatCd(null);
            searchResultEntity.setDataStatCd("20");
            searchResultRepository.save(searchResultEntity);
        }

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
        searchResultRepository.stat_co_del(tsrUno);
    }

    public void setTrkHistMemo(Integer tsrUno, String memo) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setTrkHistMemo(memo);
        searchResultRepository.save(searchResultEntity);
    }

    public void setTrkStatCd(int userUno,String userId, Integer tsrUno, String trkStatCd) {
        if(trkStatCd.equals("20")) { // 삭제요청
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);

            DeleteReqHistEntity deleteReqHistEntity = new DeleteReqHistEntity();
            deleteReqHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            deleteReqHistEntity.setUserId(userId);
            deleteReqHistEntity.setTdrTsrUno(tsrUno);
            deleteReqHistEntity.setUserUno(userUno);

            searchResultRepository.save(searchResultEntity);
            deleteReqHistRepository.save(deleteReqHistEntity);
        } else if(trkStatCd.equals("30")) { // 삭제완료
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);

            DeleteComptHistEntity deleteComptHistEntity = new DeleteComptHistEntity();
            deleteComptHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            deleteComptHistEntity.setTdcTsrUno(tsrUno);
            deleteComptHistEntity.setUserUno(userUno);
            deleteComptHistEntity.setUserId(userId);

            searchResultRepository.save(searchResultEntity);
            deleteComptHistRepository.save(deleteComptHistEntity);
        } else {
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);
            searchResultRepository.save(searchResultEntity);
        }
    }

    public void setMonitoringCd(int userUno,String userId, Integer tsrUno) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setMonitoringCd(Consts.MONITORING_CD_NONE.equals(searchResultEntity.getMonitoringCd()) ? Consts.MONITORING_CD_ING : Consts.MONITORING_CD_NONE);
        searchResultRepository.save(searchResultEntity);

        AlltimeMonitoringHistEntity alltimeMonitoringHistEntity = new AlltimeMonitoringHistEntity();
        alltimeMonitoringHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        alltimeMonitoringHistEntity.setTsrUno(tsrUno);
        alltimeMonitoringHistEntity.setUserUno(userUno);
        alltimeMonitoringHistEntity.setUserId(userId);

        if(searchResultEntity.getMonitoringCd().equals("10")){ // 활성화 될 때
            alltimeMonitoringHistEntity.setTamYn("Y");
        } else {
            alltimeMonitoringHistEntity.setTamYn("N");
        }
        alltimeMonitoringHistRepository.save(alltimeMonitoringHistEntity);
    }

    public Map<String, Object> getSearchInfoList(Integer page, String keyword) {
        log.info("getSearchInfoList page: " + page);
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, pageRequest);
        //  Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, pageRequest);

        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getSearchInfoResultCnt("10","0", keyword, pageRequest);
        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    // admin 아닐 때
    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, userUno, pageRequest);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, userUno, pageRequest);
        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getUserSearchInfoList("10","0", keyword, userUno, pageRequest);
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

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getUserUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getUserIdByTsiUnoMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByTsiUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getTsiUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getProgressPercentMap() {
        List<SearchJobRepository.ProgressPercentDtoInterface> progressPercentList = searchJobRepository.progressPercentByAll();
        Map<Integer, String> progressPercentMap = new HashMap<>();

        // progressPercentList => progressPercentMap
        for (SearchJobRepository.ProgressPercentDtoInterface item : progressPercentList) {
            progressPercentMap.put(item.getTsiUno(), String.format("%d%%", item.getProgressPercent()));
        }

        return progressPercentMap;
    }

    /**
     * 검색 결과 사이트 URL 목록 조회
     *
     * @param tsiUno (검색 정보 테이블의 key)
     * @return List<String> (검색 결과 사이트 URL List)
     */
    public List<String> findTsrSiteUrlDistinctByTsiUno(Integer tsiUno) {
        return searchResultRepository.findTsrSiteUrlDistinctByTsiUno(tsiUno);
    }

    public void searchInfoHistInsert(int userUno, String userId, String searchKeyword, String traceKeyword) {
        SearchInfoHistEntity she = new SearchInfoHistEntity();
        she.setUserUno(userUno);
        she.setUserId(userId);
        she.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        she.setHisKeyword(searchKeyword);

        if(searchKeyword.equals("")) {
            she.setHisKeyword(traceKeyword);
        }
        searchInfoHistRepository.save(she);
    }

    public void traceHistInsert(int userUno,String userId, String keyword) {
        TraceHistEntity the = new TraceHistEntity();
        the.setUserUno(userUno);
        the.setHistKeyword(keyword);
        the.setUserId(userId);
        the.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        traceHistRepository.save(the);
    }

    public void searchResultHistInsert(int userUno, String userId, int histTsiUno) {
        SearchResultHistEntity srh = new SearchResultHistEntity();
        srh.setUserUno(userUno);
        srh.setUserId(userId);
        srh.setHisTsiUno(histTsiUno);
        srh.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        searchResultHistRepository.save(srh);
    }

    public void noticeHistInsert(int userUno, String userId) {
        NoticeHistEntity nhe = new NoticeHistEntity();
        nhe.setUserUno(userUno);
        nhe.setUserId(userId);
        nhe.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        noticeHistRepository.save(nhe);
    }

    public void searchHistoryExcel(HttpServletResponse response
            , List<SearchHistoryExcelDto> searchHistoryExcelDtoList) throws IOException{
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("검색 이력");
        Row row;
        Cell cell;
        int rowNum = 0;

        row=sheet.createRow(rowNum++);

        String[] columnHeader = {"순번", "사용자 아이디", "검색타입", "키워드", "구글", "페이스북", "트위터", "인스타그램", "검색날짜"};
        for(int i=0; i<columnHeader.length; i++){
            cell=row.createCell(i);
            cell.setCellValue(columnHeader[i]);
        }

        for(int i=0; i<searchHistoryExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiUno());

            cell = row.createCell(1);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiType());

            cell = row.createCell(3);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getKeyword());

            cell = row.createCell(4);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getGoogle());

            cell = row.createCell(5);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getFacebook());

            cell = row.createCell(6);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTwitter());

            cell = row.createCell(7);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getInstagram());

            cell = row.createCell(8);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getFstDmlDt());

        }

        String fileName = "검색 이력";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();

    }

    public void resultExcelList(HttpServletResponse response, List<ResultListExcelDto> resultListExcelDtoList) throws IOException {
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("검색 결과");
        Row row;
        Cell cell;
        int rowNum = 0;

        row=sheet.createRow(rowNum++);

        String[] columnHeader = {"검색이력번호", "결과번호", "SNS", "키워드", "제목", "사이트 URL", "일치율", "사용자 아이디"};
        for(int i=0; i<columnHeader.length; i++){
            cell=row.createCell(i);
            cell.setCellValue(columnHeader[i]);
        }

        for(int i=0; i<resultListExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsiUno());

            cell = row.createCell(1);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrUno());

            cell = row.createCell(2);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrSns());

            cell = row.createCell(3);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsiKeyword());

            cell = row.createCell(4);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrTitle());

            cell = row.createCell(5);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrSiteUrl());

            cell = row.createCell(6);
            cell.setCellValue(resultListExcelDtoList.get(i).getTmrSimilarity());

            cell = row.createCell(7);
            cell.setCellValue(resultListExcelDtoList.get(i).getUserId());

        }

        String fileName = "검색 결과";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
    }

    public void deleteTsrUno(Integer tsrUno) {
        SearchResultEntity sre =  searchResultRepository.findByTsrUno(tsrUno);
        sre.setTrkStatCd(null);
        sre.setDataStatCd("20");
        searchResultRepository.save(sre);
    }

    public Map<String, Object> getUserSearchHistoryList(Integer page, String searchKeyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<UserSearchHistoryDtoInterface> userSearchHistoryList = searchInfoRepository.getUserSearchHistoryList(pageRequest, searchKeyword);

        outMap.put("userSearchHistoryList", userSearchHistoryList);
        outMap.put("totalPages", userSearchHistoryList.getTotalPages());
        outMap.put("number", userSearchHistoryList.getNumber());
        outMap.put("totalElements", userSearchHistoryList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }
}