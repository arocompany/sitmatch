package com.nex.serpServices.controller;

import com.nex.common.Consts;
import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.service.NationService;
import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.service.SerpServicesService;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/serp")
public class SerpServicesController {
    private final SerpServicesService serpServicesService;

    @GetMapping("/serviceCodeUpdate")
    public ModelAndView serviceCodeList() {
        ModelAndView modelAndView = new ModelAndView("html/serpServices");

        List<SerpServicesEntity> serviceList = serpServicesService.serpServicesList();
        modelAndView.addObject("list", serviceList);

        return modelAndView;
    }

    @PostMapping("/serviceCodeUpdate/{ssUno}/{ssIsActive}")
    public String serviceCodeUpdate(@PathVariable("ssUno") Integer ssUno, @PathVariable("ssIsActive") Integer ssIsActive) {
        if(ssUno == null || ssIsActive == null){
            log.info("ssUno: {}, ssIsActive {}", ssUno, ssIsActive);
            return "data is wrong";
        }
        return serpServicesService.serviceCodeUpdate(ssUno, ssIsActive);
    }


}
