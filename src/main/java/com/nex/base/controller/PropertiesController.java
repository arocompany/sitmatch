package com.nex.base.controller;

import com.nex.common.ConfigData;
import com.nex.common.InitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/props")
public class PropertiesController {
    private final InitService initService;


    @RequestMapping(value = "/changeProps", method = RequestMethod.POST)
    public int changeProps(@ModelAttribute ConfigData configData){
        initService.saveConfig(configData);
        return 1;
    }
}
