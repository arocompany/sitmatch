package com.nex.base.controller;

import com.nex.Chart.repo.SearchInfoHistRepository;
import com.nex.common.Consts;
import com.nex.search.entity.SearchInfoParamsEntity;
import com.nex.search.entity.VideoInfoEntity;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import com.nex.search.entity.dto.ResultCntQueryDtoInterface;
import com.nex.search.repo.SearchInfoParamsRepository;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.repo.VideoInfoRepository;
import com.nex.search.service.SearchService;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import com.nex.user.entity.SessionInfoDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class HistoryController {
    private final SearchService searchService;
    private final SearchInfoRepository searchInfoRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SearchInfoParamsRepository searchInfoParamsRepository;
    private final SearchInfoHistRepository searchInfoHistRepository;
    @GetMapping("/history")
    public ModelAndView history(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "") String searchUserFile,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                @RequestParam(required = false, defaultValue = "검색어") String manageType,
                                @RequestParam(required = false, defaultValue = "0") Integer tsiSearchType) {

        ModelAndView modelAndView = new ModelAndView("html/history");
        modelAndView.addObject("manageType", manageType);
        Map<String, Object> searchHistMap;

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("tsiSearchType", tsiSearchType);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, tsiSearchType, manageType, searchUserFile);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, userUno, tsiSearchType, manageType, searchUserFile);
        }

        if(searchUserFile != null && !searchUserFile.isEmpty()){
            modelAndView.addObject("searchKeyword", searchUserFile);
        } else {
            modelAndView.addObject("searchKeyword", searchKeyword);
        }

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));
//        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());

        List<ResultCntQueryDtoInterface> list = ((Page<ResultCntQueryDtoInterface>)searchHistMap.get("searchInfoList")).getContent();
        List<Integer> tsiUnoList = list.stream().map(ResultCntQueryDtoInterface::getTsiUno).toList();
        Map<Integer, List<VideoInfoEntity>> videoList = new HashMap<>();
        for(ResultCntQueryDtoInterface info : list){
            videoList.put(info.getTsiUno(), videoInfoRepository.findAllByTsiUno(info.getTsiUno()));
        }
        modelAndView.addObject("videoList", videoList);
        return modelAndView;
    }

    @GetMapping("/trace/history")
    public ModelAndView traceHistory(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                @RequestParam(required=false, defaultValue = "0") String traceHistoryValue,
                                @RequestParam(required = false, defaultValue = "검색어") String manageType,
                                @RequestParam(required = false, defaultValue = "0") String monitoringStatus,
                                @RequestParam(required = false, defaultValue = "0") Integer tsiUno,
                                @RequestParam(required = false, defaultValue = "0") Integer tsiSearchType) {
        ModelAndView modelAndView = new ModelAndView("html/traceHistory");
        modelAndView.addObject("manageType", manageType);

        if (manageType.equals("검색어")) {
            manageType = "1";
        } else {
            manageType = "2";
        }

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("tsiSearchType", tsiSearchType);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        Map<String, Object> traceHistoryMap;
        if(monitoringStatus.equals("0")){
            // 검색어(타이틀) 검색
            if(manageType.equals("1")){
                traceHistoryMap = searchService.getTraceHistoryList(tracePage, traceKeyword, tsiSearchType);
            } else {
                // 사례번호 검색
                traceHistoryMap = searchService.getTraceHistoryUserFileList(tracePage, traceKeyword, tsiSearchType);
            }
        }else {
            // monitoringStatus -> 10:모니터링  20:삭제요청  30:삭제완료  40:24시간모니터링
            if(manageType.equals("1")) {
                if(monitoringStatus.equals("10")){
                    if(tsiUno == null || tsiUno == 0) traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword, tsiSearchType);
                    else traceHistoryMap = searchService.getTraceHistoryMonitoringTsiUnoList(tracePage, traceKeyword, tsiUno, tsiSearchType);
                } else if(monitoringStatus.equals("20")){
                    if(tsiUno == null || tsiUno == 0) traceHistoryMap = searchService.getTraceHistoryDeleteReqList(tracePage, traceKeyword, tsiSearchType);
                    else traceHistoryMap = searchService.getTraceHistoryDeleteReqTsiUnoList(tracePage, traceKeyword, tsiUno, tsiSearchType);
                } else if(monitoringStatus.equals("30")){
                    if(tsiUno == null || tsiUno == 0) traceHistoryMap = searchService.getTraceHistoryDeleteComptList(tracePage, traceKeyword, tsiSearchType);
                    else traceHistoryMap = searchService.getTraceHistoryDeleteComptTsiUnoList(tracePage, traceKeyword, tsiUno, tsiSearchType);
                } else {
                    if(tsiUno == null || tsiUno == 0) traceHistoryMap = searchService.allTimeMonitoringList(tracePage, traceKeyword, tsiSearchType);
                    else traceHistoryMap = searchService.allTimeMonitoringTsiUnoList(tracePage, traceKeyword, tsiUno, tsiSearchType);
                }
            } else {
                if(monitoringStatus.equals("10")){
                    traceHistoryMap = searchService.getTraceHistoryMonitoringUserFileList(tracePage, traceKeyword, tsiSearchType);
                } else if(monitoringStatus.equals("20")){
                    traceHistoryMap = searchService.getTraceHistoryDeleteReqUserFileList(tracePage, traceKeyword, tsiSearchType);
                } else if(monitoringStatus.equals("30")){
                    traceHistoryMap = searchService.getTraceHistoryDeleteComptUserFileList(tracePage, traceKeyword, tsiSearchType);
                } else {
                    traceHistoryMap = searchService.allTimeMonitoringUserFileList(tracePage, traceKeyword, tsiSearchType);
                }
            }
        }

        modelAndView.addObject("traceHistoryList", Objects.requireNonNull(traceHistoryMap).get("traceHistoryList"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);
        modelAndView.addObject("monitoringStatus", monitoringStatus);

        modelAndView.addObject("traceHistoryListCount", searchService.getResultByTrace(tsiSearchType));
        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링
//        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());

        return modelAndView;
    }

    @GetMapping("/result")
    public ModelAndView result(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                               @RequestParam(value = "tsiUno") Optional<Integer> tsiUno,
                               @RequestParam(value = "tsiKeyword") Optional<String> tsiKeyword,
                               @RequestParam(required = false, defaultValue = "1") Integer page,
                               @RequestParam(required = false, defaultValue = "") String keyword,
                               @RequestParam(required = false, defaultValue = "list") String listType,
                               @RequestParam(required = false, defaultValue = "") String tsjStatusAll,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus1,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus2,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus3,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus4,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus11,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus01,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus00,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus10,
                               @RequestParam(required = false, defaultValue = "") String odStatusAll,
                               @RequestParam(required = false, defaultValue = "") String odStatus01,
                               @RequestParam(required = false, defaultValue = "") String odStatus02,
                               @RequestParam(required = false, defaultValue = "") String odStatus03,
                               @RequestParam(required = false, defaultValue = "") String snsStatusAll,
                               @RequestParam(required = false, defaultValue = "") String snsStatus01,
                               @RequestParam(required = false, defaultValue = "") String snsStatus02,
                               @RequestParam(required = false, defaultValue = "") String snsStatus03,
                               @RequestParam(required = false, defaultValue = "") String snsStatus04,
                               @RequestParam(required = false, defaultValue = "1") String priority,
                               @RequestParam(required = false, defaultValue = "0") String isImage,
                               @RequestParam(required = false, defaultValue = "") List<String> nationCode) {
        ModelAndView modelAndView = new ModelAndView("html/result");
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = null;

        log.debug("priority => {}", priority);
        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();

        if(tsiUno.isPresent()){
            int histTsiUno = tsiUno.get();
            searchService.searchResultHistInsert(userUno, userId, histTsiUno);
        }

        if (!StringUtils.hasText(tsjStatusAll)
                && !StringUtils.hasText(tsjStatus00) // 대기중
                && !StringUtils.hasText(tsjStatus01) // 처리중
                && !StringUtils.hasText(tsjStatus10) // SKIP
                && !StringUtils.hasText(tsjStatus11)) { // 일치율
            tsjStatusAll = "1";
        }

        if (!StringUtils.hasText(odStatusAll)
                && !StringUtils.hasText(odStatus01)
                && !StringUtils.hasText(odStatus02)
                && !StringUtils.hasText(odStatus03)) {
            odStatusAll = "1";
        }

        if (!StringUtils.hasText(snsStatusAll)
                && !StringUtils.hasText(snsStatus01)
                && !StringUtils.hasText(snsStatus02)
                && !StringUtils.hasText(snsStatus03)
                && !StringUtils.hasText(snsStatus04)) {
            snsStatusAll = "1";
        }

        SearchInfoParamsEntity searchInfoParamsEntity = searchInfoParamsRepository.findByTsiUno(tsiUno.get());

        if(nationCode == null || nationCode.size() == 0){
            if(searchInfoParamsEntity != null) {
                if (searchInfoParamsEntity.getTsiIsNationKr() > 0) nationCode.add("kr");
                if (searchInfoParamsEntity.getTsiIsNationUs() > 0) nationCode.add("us");
                if (searchInfoParamsEntity.getTsiIsNationCn() > 0) nationCode.add("cn");
                if (searchInfoParamsEntity.getTsiIsNationNl() > 0) nationCode.add("nl");
                if (searchInfoParamsEntity.getTsiIsNationTh() > 0) nationCode.add("th");
                if (searchInfoParamsEntity.getTsiIsNationVn() > 0) nationCode.add("vn");
                if (searchInfoParamsEntity.getTsiIsNationRu() > 0) nationCode.add("ru");
            }else{
                nationCode.add("kr");
                nationCode.add("us");
                nationCode.add("cn");
                nationCode.add("nl");
                nationCode.add("th");
                nationCode.add("vn");
                nationCode.add("ru");

                searchInfoParamsEntity = new SearchInfoParamsEntity();
                searchInfoParamsEntity.setTsiIsNationKr(1);
                searchInfoParamsEntity.setTsiIsNationUs(1);
                searchInfoParamsEntity.setTsiIsNationCn(1);
                searchInfoParamsEntity.setTsiIsNationNl(1);
                searchInfoParamsEntity.setTsiIsNationTh(1);
                searchInfoParamsEntity.setTsiIsNationVn(1);
                searchInfoParamsEntity.setTsiIsNationRu(1);

            }
        }

        modelAndView.addObject("searchInfoParams", searchInfoParamsEntity);
        modelAndView.addObject("nationCode", nationCode);

        modelAndView.addObject("tsjStatusAll", tsjStatusAll); // 분류
        modelAndView.addObject("odStatusAll", odStatusAll);   // 일치율 높은순
        modelAndView.addObject("snsStatusAll", snsStatusAll); // SNS

        if(tsiUno.isPresent()) {
            modelAndView.addObject("tsiUno", tsiUno.get());
            modelAndView.addObject("imgSrc", searchService.getSearchInfoImgUrl(tsiUno.get()));
            modelAndView.addObject("tsiType", searchService.getSearchInfoTsiType(tsiUno.get()));

            if ("1".equals(tsjStatus11)) {
                tsjStatus1 = "11";
            }
            if ("1".equals(tsjStatus01)) {
                tsjStatus2 = "01";
            }
            if ("1".equals(tsjStatus00)) {
                tsjStatus3 = "00";
            }
            if ("1".equals(tsjStatus10)) {
                tsjStatus4 = "10";
            }

            if ("1".equals(snsStatus01)) {
                snsStatus01 = "11";
            }
            if ("1".equals(snsStatus02)) {
                snsStatus02 = "13";
            }
            if ("1".equals(snsStatus03)) {
                snsStatus03 = "15";
            }
            if ("1".equals(snsStatus04)) {
                snsStatus04 = "17";
            }

            modelAndView.addObject("tsjStatus11", tsjStatus11);//일치율
            modelAndView.addObject("tsjStatus01", tsjStatus01);//처리중
            modelAndView.addObject("tsjStatus00", tsjStatus00);//대기중
            modelAndView.addObject("tsjStatus10", tsjStatus10);//SKIP

            modelAndView.addObject("odStatus01", odStatus01);//이미지
            modelAndView.addObject("odStatus02", odStatus02);//오디오
            modelAndView.addObject("odStatus03", odStatus03);//텍스트

            modelAndView.addObject("snsStatus01", snsStatus01);//구글
            modelAndView.addObject("snsStatus02", snsStatus02);//트위터
            modelAndView.addObject("snsStatus03", snsStatus03);//인스타
            modelAndView.addObject("snsStatus04", snsStatus04);//페이스북

            modelAndView.addObject("isImage", isImage);

            if(!"".equals(tsjStatusAll)) {
                tsjStatus1 = "00";
                tsjStatus2 = "01";
                tsjStatus3 = "10";
                tsjStatus4 = "11";
            }

            if(!"".equals(odStatusAll)) {
                odStatus01 = "1";
                odStatus02 = "1";
                odStatus03 = "1";
            }

            if(!"".equals(snsStatusAll)) {
                snsStatus01 = "11";
                snsStatus02 = "13";
                snsStatus03 = "15";
                snsStatus04 = "17";
            }

            String order_type = "";

            if("1".equals(odStatus01) && "1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "0"; // 모두선택 또는 전체
            }else if("1".equals(odStatus01) && !"1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "1"; // 이미지만선택
            }else if(!"1".equals(odStatus01) && "1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "2"; // 오디오만선택
            }else if(!"1".equals(odStatus01) && !"1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "3"; // 텍스트만선택
            }else if("1".equals(odStatus01) && "1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "4"; //이미지, 오디오 선택
            }else if("1".equals(odStatus01) && !"1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "5"; //이미지, 텍스트 선택
            }

            defaultQueryDtoInterface = searchService.getSearchResultList(tsiUno.get(), keyword, page, priority, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, order_type, nationCode);
        }

        tsiKeyword.ifPresent(s -> modelAndView.addObject("tsiKeyword", s));
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultList", defaultQueryDtoInterface);
        assert defaultQueryDtoInterface != null;
        modelAndView.addObject("searchResultListCount", defaultQueryDtoInterface.getTotalElements());
        modelAndView.addObject("number", defaultQueryDtoInterface.getNumber());
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);
        modelAndView.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());
        modelAndView.addObject("listType", listType);
        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("tsiType", searchInfoRepository.getSearchInfoTsiType(tsiUno.get()));
        modelAndView.addObject("searchType", searchInfoRepository.getTsiSearchType(tsiUno.get()));
        modelAndView.addObject("userId", searchService.getUserIdByTsiUnoMap().get(tsiUno.get()));
        modelAndView.addObject("searchInfo", searchInfoRepository.findByTsiUno(tsiUno.get()));

        return modelAndView;
    }

    @GetMapping("/result-detail")
    public ModelAndView result_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                      @RequestParam Integer tsrUno) {

        ModelAndView modelAndView = new ModelAndView("html/result-detail");

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultInfo", searchService.getResultInfo(tsrUno));

        return modelAndView;
    }

    @GetMapping("/result-detail2")
    public ModelAndView result_detail_for_tsi_uno(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                  @RequestParam Integer tsiUno) {

        ModelAndView modelAndView = new ModelAndView("html/result-detail2");

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultInfo", searchService.getSearchInfo(tsiUno));

        return modelAndView;
    }

    @GetMapping("/searchHistory")
    public void searchHistoryExcel(HttpServletResponse response,
                                   @RequestParam(required = false, defaultValue = "0") String searchType,
                                   @RequestParam(required = false, defaultValue = "검색어") String manageType,
                                   @RequestParam(required = false, defaultValue = "") String keyword ) throws IOException {
        List<SearchHistoryExcelDto> searchHistoryExcelDtoList = searchInfoHistRepository.searchHistoryExcelList(searchType, manageType, keyword);
        searchService.searchHistoryExcel(response, searchHistoryExcelDtoList);
    }
    
    @GetMapping("/resultExcelList")
    public void resultExcelList(HttpServletResponse response, String tsiUno, String tsiKeyword) throws IOException {
        log.info(tsiKeyword);
        log.info(tsiUno);

        List<ResultListExcelDto> resultListExcelDtoList = searchInfoHistRepository.resultExcelList(tsiUno, tsiKeyword);
        searchService.resultExcelList(response, resultListExcelDtoList);
    }

}
