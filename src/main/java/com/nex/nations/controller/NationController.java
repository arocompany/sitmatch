package com.nex.nations.controller;

import com.nex.common.Consts;
import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.service.NationService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nations")
public class NationController {
    private final NationService nationService;

    @GetMapping("/setting")
    public ModelAndView keyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/nations");
        List<NationCodeEntity> nationActiveList = nationService.nationList();

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("list", nationActiveList);
        return modelAndView;
    }

    @PostMapping("/nationCodeUpdate/{ncUno}/{ncIsActive}")
    public String nationUpdate(@PathVariable("ncUno") Integer ncUno, @PathVariable("ncIsActive") Integer ncIsActive){
        if(ncUno == null || ncIsActive == null){
            return "data is wrong";
        }
        return nationService.nationUpdate(ncUno, ncIsActive);
    }

    @PostMapping("/nationCodeUpdate/{ncIsActive}")
    public String nationAllUpdate(@PathVariable("ncIsActive") Integer ncIsActive) {
        if(ncIsActive == null){
            return "data is wrong";
        }
        return nationService.nationAllUpdate(ncIsActive);
    }
}
