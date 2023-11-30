package com.nex.Chart.controller;

import com.nex.Chart.dto.*;
import com.nex.Chart.repo.*;
import com.nex.common.Consts;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.service.SearchService;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class ChartController {
    private final SearchInfoHistRepository searchInfoHistRepository;
    private final TraceHistRepository traceHistRepository;
    private final SearchResultHistRepository searchResultHistRepository;
    private final NoticeHistRepository noticeHistRepository;
    private final SearchInfoRepository searchInfoRepository;
    private final MonitoringHistRepository monitoringHistRepository;
    private final DeleteReqHistRepository deleteReqHistRepository;
    private final DeleteComptHistRepository deleteComptHistRepository;
    private final AlltimeMonitoringHistRepository alltimeMonitoringHistRepository;

    private final UserService userService;
    private final SearchService searchService;

    @GetMapping("/prcuse")
    public ModelAndView user_prcuse(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/prcuse");
        log.info("prcuse 진입");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        String fromDate = format.format(calendar.getTime());
        String toDate = format.format(now);
        String toDate2 = toDate+" 23:59:59";

        log.info("toDate " + toDate);
        log.info("fromDate " + fromDate);

        List<SearchInfoHistDto> searchInfoHistDtoList = searchInfoHistRepository.countByClkSearchInfo(fromDate, toDate2);
        List<TraceHistDto> traceHistEntityList = traceHistRepository.countByClkTrace(fromDate, toDate2);
        List<SearchResultHistDto> searchResultHistDtoList = searchResultHistRepository.countByClkSearchResult(fromDate, toDate2);
        List<NoticeHistDto> noticeHistDtoList = noticeHistRepository.countByClkNotice(fromDate, toDate2);
        List<MonitoringHistDto> monitoringHistDtoList =monitoringHistRepository.monitoringHistList(fromDate, toDate2);
        List<DeleteReqHistDto> deleteReqHistDtoList = deleteReqHistRepository. deleteReqHistList(fromDate, toDate2);
        List<DeleteComptHistDto> deleteComptHistDtoList = deleteComptHistRepository.deleteComptHistList(fromDate, toDate2);


        modelAndView.addObject("searchInfoHistDtoList",searchInfoHistDtoList);
        modelAndView.addObject("traceHistEntityList",traceHistEntityList);
        modelAndView.addObject("searchResultHistDtoList",searchResultHistDtoList);
        modelAndView.addObject("noticeHistDtoList", noticeHistDtoList);
        modelAndView.addObject("monitoringHistDtoList", monitoringHistDtoList);
        modelAndView.addObject("deleteReqHistDtoList", deleteReqHistDtoList);
        modelAndView.addObject("deleteComptHistDtoList", deleteComptHistDtoList);


        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "prcuse");

        return modelAndView;
    }

    @PostMapping("/prcuse")
    public ModelAndView user_prcuse_post(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , String fromDate, String toDate) {
        log.info(" ==== prcuse POST 매핑 진입 ===");
        ModelAndView modelAndView = new ModelAndView("html/prcuse");

        String toDate2 = toDate + " 23:59:59";

        List<SearchInfoHistDto> searchInfoHistDtoList2 = searchInfoHistRepository.countByClkSearchInfo(fromDate, toDate2);
        List<TraceHistDto> traceHistEntityList2 = traceHistRepository.countByClkTrace(fromDate, toDate2);
        List<SearchResultHistDto> searchResultHistDtoList = searchResultHistRepository.countByClkSearchResult(fromDate, toDate2);
        List<NoticeHistDto> noticeHistDtoList = noticeHistRepository.countByClkNotice(fromDate, toDate2);

        // 모니터링 클릭 갯수, 삭제요청 갯수, 삭제완료 갯수
        List<MonitoringHistDto> monitoringHistDtoList =monitoringHistRepository.monitoringHistList(fromDate, toDate2);
        List<DeleteReqHistDto> deleteReqHistDtoList = deleteReqHistRepository. deleteReqHistList(fromDate, toDate2);
        List<DeleteComptHistDto> deleteComptHistDtoList = deleteComptHistRepository.deleteComptHistList(fromDate, toDate2);

        modelAndView.addObject("searchInfoHistDtoList",searchInfoHistDtoList2);
        modelAndView.addObject("traceHistEntityList",traceHistEntityList2);
        modelAndView.addObject("searchResultHistDtoList",searchResultHistDtoList);
        modelAndView.addObject("noticeHistDtoList", noticeHistDtoList);
        modelAndView.addObject("monitoringHistDtoList",monitoringHistDtoList);
        modelAndView.addObject("deleteReqHistDtoList", deleteReqHistDtoList);
        modelAndView.addObject("deleteComptHistDtoList", deleteComptHistDtoList);

        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "prcuse");

        return modelAndView;
    }

    @GetMapping("/connect")
    public ModelAndView connect_user(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/connect");
        log.info("connect 진입");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);


        String toDate = format.format(now);
        // String fromDate = format.format(calendar.getTime());
        // String toDate2 = toDate+" 23:59:59";

        List<TraceHistDto> userTraceHistList = traceHistRepository.userTraceHistList(toDate);
        List<SearchInfoHistDto> userSearchInfoHistList = searchInfoHistRepository.userSearchInfoHistList(toDate);
        List<SearchResultHistDto> userSearchResultHistList = searchResultHistRepository.searchResultHistList(toDate);
        List<NoticeHistDto> userNoticeHistList = noticeHistRepository.userNoticeHistList(toDate);

        List<MonitoringHistDto> userMonitoringHistList = monitoringHistRepository.userMonitoringHistList(toDate);
        List<DeleteReqHistDto> userDeleteReqHistList = deleteReqHistRepository.userDeleteReqHistList(toDate);
        List<DeleteComptHistDto> userDeleteComptHistList = deleteComptHistRepository.userDeleteComptHistList(toDate);


        modelAndView.addObject("userTraceHistList", userTraceHistList);
        modelAndView.addObject("userSearchInfoHistList", userSearchInfoHistList);
        modelAndView.addObject("userSearchResultHistList", userSearchResultHistList);
        modelAndView.addObject("userNoticeHistList", userNoticeHistList);

        modelAndView.addObject("userMonitoringHistList", userMonitoringHistList);
        modelAndView.addObject("userDeleteReqHistList", userDeleteReqHistList);
        modelAndView.addObject("userDeleteComptHistList", userDeleteComptHistList);

        // modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "connect");

        return modelAndView;
    }

    @PostMapping("/connect")
    public ModelAndView connectUser(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,  String toDate) {
        ModelAndView modelAndView = new ModelAndView("html/connect");
        log.info("connect 진입: "+toDate);

        List<TraceHistDto> userTraceHistList = traceHistRepository.userTraceHistList(toDate);
        List<SearchInfoHistDto> userSearchInfoHistList = searchInfoHistRepository.userSearchInfoHistList(toDate);
        List<SearchResultHistDto> userSearchResultHistList = searchResultHistRepository.searchResultHistList(toDate);
        List<NoticeHistDto> userNoticeHistList = noticeHistRepository.userNoticeHistList(toDate);

        List<MonitoringHistDto> userMonitoringHistList = monitoringHistRepository.userMonitoringHistList(toDate);
        List<DeleteReqHistDto> userDeleteReqHistList = deleteReqHistRepository.userDeleteReqHistList(toDate);
        List<DeleteComptHistDto> userDeleteComptHistList = deleteComptHistRepository.userDeleteComptHistList(toDate);

        modelAndView.addObject("userTraceHistList", userTraceHistList);
        modelAndView.addObject("userSearchInfoHistList", userSearchInfoHistList);
        modelAndView.addObject("userSearchResultHistList", userSearchResultHistList);
        modelAndView.addObject("userNoticeHistList", userNoticeHistList);

        modelAndView.addObject("userMonitoringHistList", userMonitoringHistList);
        modelAndView.addObject("userDeleteReqHistList", userDeleteReqHistList);
        modelAndView.addObject("userDeleteComptHistList", userDeleteComptHistList);

        // modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "connect");

        return modelAndView;
    }

    @GetMapping("/connectCnt")
    public List<LoginHistDto> connectCnt(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) {
        String toDate2 = toDate + " 23:59:59";
        // List<LoginHistDto> userHistoryCountList2 = userService.getUserHistory2(fromDate, toDate2);
        return userService.getUserHistory2(fromDate, toDate2);
    }

    @GetMapping("/userLoginCnt")
    public List<LoginHistDto> usreLoginCnt(@RequestParam("toDate") String toDate) {
        return userService.getUserHistory(toDate);
    }

    @GetMapping("/excel/download")
    public void excelUserHistoryDownload(HttpServletResponse response, String fromDate, String toDate) throws IOException {
        log.info("excelUserHistoryDownload 진입");
        String toDate2 = toDate + " 23:59:59";

        List<LoginExcelDto> loginExcelDtoList = userService.userHistExcel(fromDate, toDate2);
        userService.excelUserHistory(response,loginExcelDtoList);
    }

    @GetMapping("/prcuse/excel/download")
    public void prcuseExcelDownload(HttpServletResponse response, String fromDate, String toDate) throws IOException {
        String toDate2 = toDate + " 23:59:59";

        List<LoginExcelDto> loginExcelDtoList = userService.userHistExcel(fromDate, toDate2);

        List<SearchInfoExcelDto> searchInfoExcelDtoList = searchInfoHistRepository.searchInfoExcelList(fromDate, toDate2);
        List<TraceHistExcelDto> traceHistExcelDtoList = traceHistRepository.traceExcelList(fromDate, toDate2);
        List<SearchResultExcelDto> searchResultExcelDtoList = searchResultHistRepository.searchResultExcelList(fromDate, toDate2);
        List<NoticeListExcelDto> noticeListExcelDtoList = noticeHistRepository.noticeExcelList(fromDate, toDate2);

        List<DateKeywordExcelDto> dateSearchInfoResultExcelList = searchInfoRepository.dateSearchInfoResultExcelList(fromDate, toDate2); // 기간별 키워드
        List<MonitoringHistExcelDto> dateMonitoringExcelList = monitoringHistRepository.dateMonitoringExcelList(fromDate, toDate2); // 모니터링 한 갯수
        List<DeleteReqHistExcelDto> dateMonitoringReqExcelList = monitoringHistRepository.dateMonitoringReqExcelList(fromDate, toDate2); // 삭제 요청한 갯수
        List<DeleteComptHistExcelDto> dateMonitoringComptExcelList = monitoringHistRepository.dateMonitoringComptExcelList(fromDate, toDate2); // 삭제 완료된 갯수

        List<AllTimeCntExcelDto> dateAlltimeMonitoringExcelList =alltimeMonitoringHistRepository.dateAllTimeCntExcelList(fromDate,toDate2);

        userService.prcuseExcel(response, searchInfoExcelDtoList, traceHistExcelDtoList, searchResultExcelDtoList, noticeListExcelDtoList
                , loginExcelDtoList,dateSearchInfoResultExcelList,dateMonitoringExcelList,dateMonitoringReqExcelList,dateMonitoringComptExcelList, dateAlltimeMonitoringExcelList);

        log.info("prcuseExcel");
    }

    @GetMapping("/connect/excel/download")
    public void connectExcelDownload(HttpServletResponse response, String toDate) throws IOException {
        log.info(" connectExcelDownload: 진입: " + toDate);
        String toDate2 = toDate + " 23:59:59";
        log.info("connectExcelDownload");

        List<SearchInfoExcelDto> searchInfoExcelDtoList = searchInfoHistRepository.searchInfoExcelList(toDate, toDate2);
        List<TraceHistExcelDto> traceHistExcelDtoList = traceHistRepository.traceExcelList(toDate, toDate2);
        List<SearchResultExcelDto> searchResultExcelDtoList = searchResultHistRepository.searchResultExcelList(toDate, toDate2);
        List<NoticeListExcelDto> noticeListExcelDtoList = noticeHistRepository.noticeExcelList(toDate, toDate2);
        List<LoginExcelDto> loginExcelDtoList = userService.userHistExcel(toDate, toDate2);

        List<SearchInfoExcelDto> userKeywordCntExcelList = searchInfoHistRepository.userKeywordCntExcelList(toDate, toDate2);
        List<MonitoringHistExcelDto> userMonitoringExcelList = monitoringHistRepository.userMonitoringExcelList(toDate); // 모니터링 한 갯수
        List<DeleteReqHistExcelDto> userDeleteReqExcelList = deleteReqHistRepository.userDeleteReqExcelList(toDate);    // 삭제 요청 갯수
        List<DeleteComptHistExcelDto> userDeleteComptExcelList = deleteComptHistRepository.userDeleteComptExcelList(toDate); // 삭제 완료 갯수

        List<AllTimeCntExcelDto> userAllTimeCntExcelList = alltimeMonitoringHistRepository.userAllTimeCntExcelList(toDate); // 24시 모니터링 그래프

        userService.connectExcel(response, searchInfoExcelDtoList, traceHistExcelDtoList, searchResultExcelDtoList, noticeListExcelDtoList
                , loginExcelDtoList, userKeywordCntExcelList, userMonitoringExcelList, userDeleteReqExcelList, userDeleteComptExcelList,userAllTimeCntExcelList);
    }

    @GetMapping("/searchHistory")
    public void searchHistoryExcel(HttpServletResponse response) throws IOException {
        List<SearchHistoryExcelDto> searchHistoryExcelDtoList = searchInfoHistRepository.searchHistoryExcelList();
        searchService.searchHistoryExcel(response, searchHistoryExcelDtoList);
    }

    @GetMapping("/resultExcelList")
    public void resultExcelList(HttpServletResponse response, String tsiUno, String tsiKeyword) throws IOException {
        log.info("========== resultExcelList 진입 ============");
        log.info("tsiUno " + tsiUno);
        log.info("tsiKeyword " + tsiKeyword);

        List<ResultListExcelDto> resultListExcelDtoList = searchInfoHistRepository.resultExcelList(tsiUno, tsiKeyword);

        searchService.resultExcelList(response, resultListExcelDtoList);
    }

/*
    @GetMapping("/keywordCntList")
    public Map<String, Object> keywordCntList(HttpServletResponse response
                               ,@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) {
        log.info(" == keywordcntList 진입 == ");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(calendar.DATE, -6);

        fromDate = format.format(calendar.getTime());
        toDate = format.format(now);
        String toDate2 = toDate+" 23:59:59";

        JsonDto jsonDto = new JsonDto();
        List<String> dateList = searchInfoRepository.searchKeywordDateCnt(fromDate, toDate2);
        List<String> keywordCntList = searchInfoRepository.searchKeywordCnt(fromDate, toDate2);
        List<String> resultCnt = searchResultRepository.keywordResultCntList(fromDate, toDate2);

        jsonDto.setDateList(dateList);
        jsonDto.setCntList(keywordCntList);
        jsonDto.setResultList(resultCnt);

        Map<String, Object> data = new HashMap<>();
        data.put("data", jsonDto);

        try {
            String json = new ObjectMapper().writeValueAsString(data);
            System.out.println(json);

        } catch (Exception e) {
            e.getMessage();
        }

        return data;
    }
 */

    @GetMapping("/keywordCntList")
    public List<KeywordCntDto> keywordCntList(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) {
        log.info(" == keywordcntList 진입 == "+fromDate +" "+toDate);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        String toDate2 = toDate+" 23:59:59";

        return searchInfoRepository.dateKeywordResultCntList(fromDate, toDate2);
    }

    @GetMapping("/userKeywordCntList")
    public List<userKeywordCntDto> userKeywordCntList(@RequestParam("toDate") String toDate) {
        log.info(" == keywordcntList 진입 == ");
        return searchInfoRepository.userKeywordCntList(toDate);
    }

    @GetMapping("/allTimeCntList")
    public List<AllTimeCntDto> allTimeCntList(@RequestParam("toDate") String toDate) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        toDate = format.format(now);
        // fromDate = format.format(calendar.getTime());
        // String toDate2 = toDate + " 23:59:59";

        return alltimeMonitoringHistRepository.allTimeCntList(toDate);
    }

    @GetMapping("/monitoringDateCntList")
    public List<AllTimeCntDto> monitoringDateCntList(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate) {

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        fromDate = format.format(calendar.getTime());
        toDate = format.format(now);
        String toDate2 = toDate + " 23:59:59";
        return alltimeMonitoringHistRepository.monitoringDateCntList(fromDate, toDate2);
    }



}
