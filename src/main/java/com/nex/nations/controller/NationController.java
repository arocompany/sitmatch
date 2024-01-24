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
        log.info("========= nations 페이지 진입 ========");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        List<NationCodeEntity> nationActiveList = nationService.nationList();

        modelAndView.addObject("list", nationActiveList);

        return modelAndView;
    }

    @PostMapping("/nationCodeUpdate")
    public String nationCodeUpdate(String ncUno, String ncIsActive){
        int ncIsActive2=0;

        int ncUno2 = Integer.parseInt(ncUno);
        if(ncIsActive.equals("1")){
            ncIsActive2 = 0;
        } else {
            ncIsActive2 = 1;
        }

        log.info("nationCodeUpdate 진입");
        log.info("ncUno: "+ncUno+" ncIsActive: "+ncIsActive);
        //nationService.nationUpdate(ncUno, ncIsActive2);
        return "success";
    }

    @PostMapping("/nationCodeAllUpdate")
    public String nationCodeAllUpdate() {
        
        return "success";
    }
}
