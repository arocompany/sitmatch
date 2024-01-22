package com.nex.serpServices.controller;

import com.nex.common.Consts;
import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.service.SerpServicesService;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/serp")
public class SerpServicesController {
    @GetMapping("/setting")
    public ModelAndView keyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/serpServices");
        log.info("========= serpServices 페이지 진입 ========");

//        List<NewKeywordDto> newKeywordList = newKeywordRepository.keywordList();

//        modelAndView.addObject("newKeywordList", newKeywordList);
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    /*
    private final SerpServicesService serpServicesService;

    @GetMapping("/servicesList")
    public ModelAndView servicesList(){
        ModelAndView modelAndView = new ModelAndView("");
        List<SerpServicesEntity> serpServicesList = serpServicesService.serpServicesList();

        modelAndView.addObject("serpServicesList",serpServicesList);

        return modelAndView;
    }
     */

}
