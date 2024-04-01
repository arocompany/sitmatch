package com.nex.base.controller;

import com.nex.common.Consts;
import com.nex.request.ReqNotice;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import com.nex.search.entity.dto.SearchResultMonitoringHistoryDto;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.repo.SearchResultMonitoringRepository;
import com.nex.search.service.SearchService;
import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.service.SerpServicesService;
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
    private final SearchResultMonitoringRepository searchResultMonitoringRepository;
    private final SerpServicesService serpServicesService;

    @GetMapping("/")
    public ModelAndView index(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView mv = new ModelAndView("html/index");
        mv.addObject("headerMenu", "index");
        List<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeListMain(0);
        List<SerpServicesEntity> serpServicesIsSsActiveList = serpServicesService.serpServicesIsSsActiveList(1);


        mv.addObject("traceInfoList", defaultQueryDtoInterface);
        mv.addObject("serpServicesIsSsActiveList", serpServicesIsSsActiveList);
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
    public ModelAndView notice(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                                , @ModelAttribute ReqNotice param, Model model
                                , @RequestParam(required = false, defaultValue = "0") Integer tsiSearchType
                                , @RequestParam(required = false, defaultValue = "list") String listType) {
        ModelAndView mv = new ModelAndView("html/notice");
        searchService.noticeHistInsert(sessionInfoDto.getUserUno(), sessionInfoDto.getUserId());
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeList(param.getPage(), param.getTsiUno(), param.getTsiKeyword(), tsiSearchType);

        mv.addObject("tsiSearchType", tsiSearchType);

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
        mv.addObject("listType", listType);
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
        searchService.deleteTsrUno(tsrUno);
        return "success";
    }

    @GetMapping("/deleteTsrUnos")
    public String deleteTsrUnos(@RequestParam(value="tsrUnoValues", required=false) List<Integer> tsrUnoValues) {
        log.info("tsrUnoValues: "+tsrUnoValues);
        searchService.deleteTsrUnos(tsrUnoValues);

        return "success";
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
        int userUno = sessionInfoDto.getUserUno();

        if(sessionInfoDto.isAdmin()) {
            userSearchHistoryList = searchService.getAllUserSearchHistoryList(searchPage, searchKeyword);
        } else {
            userSearchHistoryList = searchService.getUserSearchHistoryList(searchPage, searchKeyword, userUno);
        }

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("userSearchHistoryList", userSearchHistoryList.get("userSearchHistoryList"));
        modelAndView.addObject("userSearchHistoryListCount", userSearchHistoryList.get("totalElements"));
        modelAndView.addObject("searchNumber", userSearchHistoryList.get("number"));
        modelAndView.addObject("maxPage", userSearchHistoryList.get("maxPage"));
        modelAndView.addObject("searchTotalPages", userSearchHistoryList.get("totalPages"));
        modelAndView.addObject("searchKeyword", searchKeyword);

        return modelAndView;
    }

    @GetMapping("/allTimeMonitoringHist")
    public ModelAndView allTimeMonitoringHist(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                @RequestParam int tsrUno) {
        ModelAndView modelAndView = new ModelAndView("html/allTimeMonitoringHist");
        List<SearchResultMonitoringHistoryDto> searchResultMonitoringHistoryList = searchResultMonitoringRepository.searchResultMonitoringHistoryList(tsrUno);

        modelAndView.addObject("searchResultMonitoringHistoryList", searchResultMonitoringHistoryList);
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }
}
