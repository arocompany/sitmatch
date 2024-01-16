package com.nex.base.controller;

import com.nex.common.Consts;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class TraceController {
    private final SearchService searchService;
    @GetMapping("/trace")
    public ModelAndView trace(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                              @RequestParam(required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = "") String trkStatCd,
                              @RequestParam(required = false, defaultValue = "") String keyword) {
        // @RequestParam(required = false, defaultValue = "list") String listType
        ModelAndView modelAndView = new ModelAndView("html/trace");

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.traceHistInsert(userUno, userId, keyword);

        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getTraceList(page, trkStatCd, keyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "trace");

        modelAndView.addObject("traceList", defaultQueryDtoInterface);
        // 페이징 관련
        modelAndView.addObject("traceListCount", defaultQueryDtoInterface.getTotalElements());
        modelAndView.addObject("number", defaultQueryDtoInterface.getNumber());
        modelAndView.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);

        // 검색어
        modelAndView.addObject("trkStatCd", trkStatCd);
        modelAndView.addObject("keyword", keyword);

        return modelAndView;
    }

    @GetMapping("/trace-detail")
    public ModelAndView trace_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                     @RequestParam Integer tsrUno) {
        ModelAndView modelAndView = new ModelAndView("html/trace-detail");
        DefaultQueryDtoInterface defaultQueryDtoInterface = searchService.getTraceInfo(tsrUno);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("traceInfo", defaultQueryDtoInterface);
        modelAndView.addObject("videoInfoList", searchService.getVideoInfoList(defaultQueryDtoInterface.getTsiUno()));

        return modelAndView;
    }
}
