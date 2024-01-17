package com.nex.base.controller;

import com.nex.Chart.dto.AllTimeMonitoringHistDto;
import com.nex.Chart.repo.AlltimeMonitoringHistRepository;
import com.nex.common.Consts;
import com.nex.request.ReqNotice;
import com.nex.search.entity.SearchResultMonitoringHistoryEntity;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import com.nex.search.entity.dto.SearchResultMonitoringHistoryDto;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultMonitoringRepository;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.repo.AutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class BaseController {

    private final SearchService searchService;
    private final AutoRepository autoRepository;
    private final SearchJobRepository searchJobRepository;
    private final AlltimeMonitoringHistRepository alltimeMonitoringHistRepository;
    private final SearchResultMonitoringRepository searchResultMonitoringRepository;


    @GetMapping("/")
    public ModelAndView index(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView mv = new ModelAndView("html/index");
        mv.addObject("headerMenu", "index");
        List<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeListMain(0);
        mv.addObject("traceInfoList", defaultQueryDtoInterface);
        mv.addObject("sessionInfo", sessionInfoDto);
        return mv;
    }

    @GetMapping("/password")
    public ModelAndView password(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto, Model model) {
        ModelAndView mv = new ModelAndView("html/password");
        mv.addObject("sessionInfo", sessionInfoDto);
        return mv;
    }

    // 카운트
    @PostMapping("/history_tsi_uno_count")
    public int history_tsi_uno_count(@RequestParam(required = false) Integer tsi_uno){
        return searchJobRepository.countByTsiUno(tsi_uno);
    }

    // 신조어 카운트
    @PostMapping("/newKeyword_tsi_uno_count")
    public int newKeyword_tsi_uno_count(@RequestParam(required = false) Integer tsi_uno){
        return searchJobRepository.countByTsiUno(tsi_uno);
    }



    @GetMapping("/notice")
    public ModelAndView notice(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto , @ModelAttribute ReqNotice param, Model model) {
        ModelAndView mv = new ModelAndView("html/notice");
        searchService.noticeHistInsert(sessionInfoDto.getUserUno(), sessionInfoDto.getUserId());
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeList(param.getPage(), param.getTsiUno(), param.getTsiKeyword());

        mv.addObject("sessionInfo", sessionInfoDto);
        mv.addObject("searchResultList", defaultQueryDtoInterface);
        mv.addObject("tsrUno", param.getTsrUno());
        assert defaultQueryDtoInterface != null;
        mv.addObject("searchResultListCount", defaultQueryDtoInterface.getTotalElements());
        mv.addObject("number", defaultQueryDtoInterface.getNumber());
        mv.addObject("maxPage", Consts.MAX_PAGE);
        mv.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());

        mv.addObject("tsrUno", param.getTsrUno());
        mv.addObject("tsiUno", param.getTsiUno());
        mv.addObject("imgSrc", searchService.getSearchInfoImgUrl(param.getTsiUno())); //tsi
        mv.addObject("tsiType", searchService.getSearchInfoTsiType(param.getTsiUno())); //tsi
        mv.addObject("userId", searchService.getUserIdByTsiUnoMap().get(param.getTsiUno())); //tsi
        mv.addObject("tsiKeyword", param.getTsiKeyword());

        return mv;
    }

    @GetMapping("/loading")
    public ModelAndView loading(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/loading");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    @PostMapping("/ajax_auto_Insert")
    public int ajax_auto_Insert(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                         @RequestParam String auto_keyword) {
        return autoRepository.auto_Insert(auto_keyword,sessionInfoDto.getUserId());
    }

    @PostMapping("/ajax_auto_Delete")
    public int ajax_auto_Delete(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam String auto_keyword) {
        return autoRepository.auto_Delete(auto_keyword,sessionInfoDto.getUserId());
    }

    // 추적이력 단건 삭제
    @PostMapping("/deleteTsrUno")
    public String deleteTsrUno(Integer tsrUno) {
        log.info("tsrUno: " + tsrUno);

        searchService.deleteTsrUno(tsrUno);

        return "success";
    }


    @GetMapping("/deleteTsrUnos")
    public String deleteTsrUnos(@RequestParam(value="tsrUnoValues", required=false) List<Integer> tsrUnoValues) {
        log.info("tsrUnoValues: "+tsrUnoValues);
        searchService.deleteTsrUnos(tsrUnoValues);

        return "success";
    }

    @GetMapping("/monitoringList")
    public ModelAndView monitoringList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                       @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                       @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                       @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                       @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                       @RequestParam(required = false, defaultValue = "") Integer tsiUno,
                                       @RequestParam(required = false, defaultValue = "전체") String manageType) {
        ModelAndView modelAndView = new ModelAndView("html/monitoringList");
        log.info("monitoringList 진입 manageType : " + manageType );

        String manageTypeValue = setManageType(manageType);
        log.info("manageTypeValue: " + manageTypeValue);

        Map<String, Object> searchHistMap;

        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }
        // int Percent = sessionInfoDto.getPercent_limit();

        // 추적이력
        Map<String, Object> traceHistoryMap;

        if(manageTypeValue.equals("0") || manageTypeValue.equals("1")) {
            if(tsiUno == null){
                log.info("tsiUno == null");
                traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword);
            } else {
                log.info("tsiUno != null");
                traceHistoryMap = searchService.getTraceHistoryMonitoringTsiUnoList(tracePage, traceKeyword, tsiUno);
            }
        } else {
            if(tsiUno == null) { // tsiUno가 없을때
                traceHistoryMap = searchService.getTraceHistoryMonitoringUserFileList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.getTraceHistoryMonitoringTsiUnoUserFileList(tracePage, traceKeyword, tsiUno);
            }
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링

        return modelAndView;
    }

    @GetMapping("/monitoringDeleteReqList")
    public ModelAndView monitoringDeleteReqList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                       @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                       @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                       @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                       @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                       @RequestParam(required = false, defaultValue = "") Integer tsiUno,
                                       @RequestParam(required = false, defaultValue = "전체") String manageType) {
        ModelAndView modelAndView = new ModelAndView("html/monitoringDeleteReqList");
        log.info("monitoringDeleteReqList 진입 " + " manageType: "+manageType);

        String manageTypeValue = setManageType(manageType);

        Map<String, Object> searchHistMap;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());

        log.info("traceKeyword: "+traceKeyword + " searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        // int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");

        Map<String, Object> traceHistoryMap;

        // 추적이력
        if(manageTypeValue.equals("0") || manageTypeValue.equals("1")) {
            if(tsiUno == null){
                traceHistoryMap = searchService.getTraceHistoryDeleteReqList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.getTraceHistoryDeleteReqTsiUnoList(tracePage, traceKeyword, tsiUno);
            }
        } else {
            if(tsiUno == null) {
                traceHistoryMap = searchService.getTraceHistoryDeleteReqUserFileList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.getTraceHistoryDeleteReqTsiUnoUserFileList(tracePage, traceKeyword, tsiUno);
            }
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/monitoringDeleteComptList")
    public ModelAndView monitoringDeleteComptList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                  @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                                  @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                                  @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                                  @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                                  @RequestParam(required = false, defaultValue = "") Integer tsiUno,
                                                  @RequestParam(required = false, defaultValue = "전체") String manageType) {
        ModelAndView modelAndView = new ModelAndView("html/monitoringDeleteComptList");
        log.info("monitoringDeleteComptList 진입 " + " manageType: "+manageType);

        String manageTypeValue = setManageType(manageType);

        Map<String, Object> searchHistMap;

        log.info(" traceKeyword: " + traceKeyword + " searchKeyword: " + searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        // int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");

        Map<String, Object> traceHistoryMap;

        // 추적이력
        if (manageTypeValue.equals("0") || manageTypeValue.equals("1")) {
            if(tsiUno == null){
                traceHistoryMap = searchService.getTraceHistoryDeleteComptList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.getTraceHistoryDeleteComptTsiUnoList(tracePage, traceKeyword, tsiUno);
            }
        } else {
            if(tsiUno == null) {
                traceHistoryMap = searchService.getTraceHistoryDeleteComptUserFileList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.getTraceHistoryDeleteComptTsiUnoUserFileList(tracePage, traceKeyword, tsiUno);
            }
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/userSearchHistory")
    public ModelAndView userSearchHistory(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                          @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                          @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/userSearchHistory");
        log.info("searchKeyword: "+searchKeyword+" page: " + searchPage);
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "userSearchHistory");

        Map<String, Object> userSearchHistoryList;

        userSearchHistoryList = searchService.getUserSearchHistoryList(searchPage, searchKeyword);

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("userSearchHistoryList", userSearchHistoryList.get("userSearchHistoryList"));
        modelAndView.addObject("userSearchHistoryListCount", userSearchHistoryList.get("totalElements"));
        modelAndView.addObject("searchNumber", userSearchHistoryList.get("number"));
        modelAndView.addObject("maxPage", userSearchHistoryList.get("maxPage"));
        modelAndView.addObject("searchTotalPages", userSearchHistoryList.get("totalPages"));
        modelAndView.addObject("searchKeyword", searchKeyword);

        // modelAndView.addObject("searchKeyword", searchKeyword);

        return modelAndView;
    }

    // 24시간 모니터링 리스트
    @GetMapping("/allTimeMonitoringChkList")
    public ModelAndView allTimeMonitoringChkList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                 @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                                 @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                                 @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                                 @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                                 @RequestParam(required = false, defaultValue = "") Integer tsiUno,
                                                 @RequestParam(required = false, defaultValue = "전체") String manageType) {
        ModelAndView modelAndView = new ModelAndView("html/allTimeMonitoringChkList");
        log.info("allTimeMonitoringChkLists 진입 " + " manageType: "+manageType);

        String manageTypeValue = setManageType(manageType);

        Map<String, Object> searchHistMap;
        log.info(" traceKeyword: " + traceKeyword + " searchKeyword: " + searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }
        // int Percent = sessionInfoDto.getPercent_limit();

        Map<String, Object> traceHistoryMap;

        // 추적이력
        if(manageTypeValue.equals("0") || manageTypeValue.equals("1")) {
            if(tsiUno == null){
                traceHistoryMap = searchService.allTimeMonitoringList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.allTimeMonitoringTsiUnoList(tracePage, traceKeyword, tsiUno);
            }
        } else {
            if(tsiUno == null){
                traceHistoryMap = searchService.allTimeMonitoringUserFileList(tracePage, traceKeyword);
            } else {
                traceHistoryMap = searchService.allTimeMonitoringTsiUnoUserFileList(tracePage, traceKeyword, tsiUno);
            }
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/allTimeMonitoringHist")
    public ModelAndView allTimeMonitoringHist(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                @RequestParam int tsrUno) {
        log.info(" === allTimeMonitoringHist 진입 === " + tsrUno);
        ModelAndView modelAndView = new ModelAndView("html/allTimeMonitoringHist");

        List<SearchResultMonitoringHistoryDto> searchResultMonitoringHistoryList = searchResultMonitoringRepository.searchResultMonitoringHistoryList(tsrUno);

        modelAndView.addObject("searchResultMonitoringHistoryList", searchResultMonitoringHistoryList);
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    public String setManageType(String manageType) {
        log.info("manageType: " + manageType);
        String manage;
        if(manageType.equals("전체")){
            manage = "0";
        } else if(manageType.equals("검색어")) {
            manage = "1";
        } else {
            manage = "2";
        }
        return manage;
    }

}
