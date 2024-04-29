package com.nex.statistics.controller;

import com.nex.Chart.dto.*;
import com.nex.Chart.repo.*;
import com.nex.common.Consts;
import com.nex.search.repo.SearchInfoRepository;
import com.nex.search.service.SearchService;
import com.nex.statistics.service.StatisticsService;
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
@RequestMapping("/statistics")
public class StatisticsController {
    private final SearchInfoRepository searchInfoRepository;
    private final StatisticsService statisticsService;


    @GetMapping()
    public ModelAndView statistics(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/statistics");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);

        String fromDate = format.format(calendar.getTime());
        String toDate = format.format(now);
        String toDate2 = toDate+" 23:59:59";

        log.info("toDate " + toDate);
        log.info("fromDate " + fromDate);

        modelAndView.addObject("statisticsSearchInfoByTsiType1", searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchResultByTsiType1", searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiType1", searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchResultMonitoringByTsiType1", searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiTypeAndUser1", searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 1));

        modelAndView.addObject("statisticsSearchInfoByTsiType2", searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchResultByTsiType2", searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiType2", searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchResultMonitoringByTsiType2", searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiTypeAndUser2", searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 2));


        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "statistics");

        return modelAndView;
    }

    @PostMapping()
    public ModelAndView statistics_post(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                                        , String fromDate, String toDate) {
        ModelAndView modelAndView = new ModelAndView("html/statistics");

        String toDate2 = toDate + " 23:59:59";

        modelAndView.addObject("statisticsSearchInfoByTsiType1", searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchResultByTsiType1", searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiType1", searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchResultMonitoringByTsiType1", searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 1));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiTypeAndUser1", searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 1));

        modelAndView.addObject("statisticsSearchInfoByTsiType2", searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchResultByTsiType2", searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiType2", searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchResultMonitoringByTsiType2", searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 2));
        modelAndView.addObject("statisticsSearchInfoMonitoringByTsiTypeAndUser2", searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 2));

        modelAndView.addObject("fromDate", fromDate);
        modelAndView.addObject("toDate", toDate);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "statistics");

        return modelAndView;
    }

    @GetMapping("/excel/download")
    public void prcuseExcelDownload(HttpServletResponse response, String fromDate, String toDate) throws IOException {
        String toDate2 = toDate + " 23:59:59";

        List<StatisticsDto> statisticsSearchInfoByTsiType1 = searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 1);
        List<StatisticsDto> statisticsSearchResultByTsiType1 = searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 1);
        List<StatisticsDto> statisticsSearchInfoMonitoringByTsiType1 = searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 1);
        List<StatisticsDto> statisticsSearchResultMonitoringByTsiType1 = searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 1);
        List<StatisticsDto> statisticsSearchInfoMonitoringByTsiTypeAndUser1 = searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 1);
        List<StatisticsDto> statisticsSearchInfoByTsiType2 = searchInfoRepository.statisticsSearchInfoByTsiType(fromDate, toDate2, 2);
        List<StatisticsDto> statisticsSearchResultByTsiType2 = searchInfoRepository.statisticsSearchResultByTsiType(fromDate, toDate2, 2);
        List<StatisticsDto> statisticsSearchInfoMonitoringByTsiType2 = searchInfoRepository.statisticsSearchInfoMonitoringByTsiType(fromDate, toDate2, 2);
        List<StatisticsDto> statisticsSearchResultMonitoringByTsiType2 = searchInfoRepository.statisticsSearchResultMonitoringByTsiType(fromDate, toDate2, 2);
        List<StatisticsDto> statisticsSearchInfoMonitoringByTsiTypeAndUser2 = searchInfoRepository.statisticsSearchInfoMonitoringByTsiTypeAndUser(fromDate, toDate2, 2);

        statisticsService.exportExcel(response
                , statisticsSearchInfoByTsiType1
                , statisticsSearchResultByTsiType1
                , statisticsSearchInfoMonitoringByTsiType1
                , statisticsSearchResultMonitoringByTsiType1
                , statisticsSearchInfoMonitoringByTsiTypeAndUser1
                , statisticsSearchInfoByTsiType2
                , statisticsSearchResultByTsiType2
                , statisticsSearchInfoMonitoringByTsiType2
                , statisticsSearchResultMonitoringByTsiType2
                , statisticsSearchInfoMonitoringByTsiTypeAndUser2
        );

        log.info("statisticsExcel");
    }
}
