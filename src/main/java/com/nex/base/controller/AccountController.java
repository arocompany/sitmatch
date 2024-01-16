package com.nex.base.controller;

import com.nex.common.Consts;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class AccountController {
    private final UserRepository userRepository;

    @GetMapping("/manage")
    public ModelAndView manage(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                               @RequestParam(required = false, defaultValue = "1") Integer page) {
        return manage(sessionInfoDto, "전체", "", page);
    }

    @PostMapping("/manage")
    public ModelAndView manage(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , @RequestParam(required = false, defaultValue = "전체") String manageType
            , @RequestParam(required = false, defaultValue = "") String keyword
            , @RequestParam(required = false, defaultValue = "1") Integer page) {
        ModelAndView modelAndView = new ModelAndView("html/manage");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "manage");

        // 검색 조건 저장
        modelAndView.addObject("manageType", manageType);
        modelAndView.addObject("keyword", keyword);

        // 페이징 처리 데이터 저장
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);

        switch (manageType) {
            case "아이디" ->
                    modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserIdContainingOrderByUserUnoDesc("99", "Y", keyword, pageRequest));
            case "이름" ->
                    modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserNmContainingOrderByUserUnoDesc("99", "Y", keyword, pageRequest));
            case "전체" ->
                    modelAndView.addObject("counselorInfoList", userRepository.findAllByEntire("99", "Y", keyword, pageRequest));

//            modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserIdContainingOrUserNmContainingOrderByUserUnoDesc("99", "Y", keyword, keyword, pageRequest));
            default ->
                    modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnOrderByUserUnoDesc("99", "Y", pageRequest));
        }
        return modelAndView;
    }

    @GetMapping("/counselor-add")
    public ModelAndView counselor_add(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/counselor-add");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    @GetMapping("/counselor-detail")
    public ModelAndView counselor_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                         @RequestParam Long userUno) {
        ModelAndView modelAndView = new ModelAndView("html/counselor-detail");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("counselorInfo", userRepository.findByUserUno(userUno));

        return modelAndView;
    }
}
