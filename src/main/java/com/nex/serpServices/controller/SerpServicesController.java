package com.nex.serpServices.controller;

import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.service.SerpServicesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/serp")
public class SerpServicesController {
    private final SerpServicesService serpServicesService;

    @GetMapping("/servicesList")
    public ModelAndView servicesList(){
        ModelAndView modelAndView = new ModelAndView("");
        List<SerpServicesEntity> serpServicesList = serpServicesService.serpServicesList();

        modelAndView.addObject("serpServicesList",serpServicesList);

        return modelAndView;
    }

}
