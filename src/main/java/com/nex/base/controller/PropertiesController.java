package com.nex.base.controller;

import com.nex.batch.ScheduleTasks;
import com.nex.common.ConfigData;
import com.nex.common.ConfigDataManager;
import com.nex.common.InitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/props")
public class PropertiesController {
    private final InitService initService;
    private final ScheduleTasks scheduleTasks;

    @RequestMapping(value = "/changeProps", method = RequestMethod.POST)
    public int changeProps(@ModelAttribute ConfigData param){
        initService.saveConfig(param);

        ConfigData configData = ConfigDataManager.getInstance().getDefaultConfig();

        if(configData.getIsBatchFlag()){
            scheduleTasks.reStartScheduler();
        }
        return 1;
    }
}
