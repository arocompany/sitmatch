package com.nex.base.controller;

import com.nex.Chart.dto.AllTimeMonitoringHistDto;
import com.nex.Chart.repo.AlltimeMonitoringHistRepository;
import com.nex.common.Consts;
import com.nex.search.entity.DefaultQueryDtoInterface;
import com.nex.search.repo.NewKeywordRepository;
import com.nex.search.repo.SearchJobRepository;
import com.nex.search.service.SearchService;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.repo.AutoRepository;
import com.nex.user.repo.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class BaseController {
    private final UserRepository userRepository;
    private final SearchService searchService;
    private final AutoRepository autoRepository;
    private final SearchJobRepository searchJobRepository;
    private final NewKeywordRepository newKeywordRepository;
    private final AlltimeMonitoringHistRepository alltimeMonitoringHistRepository;


    @GetMapping("/")
    public ModelAndView index(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/index");
        modelAndView.addObject("headerMenu", "index");

        log.info("sessionInfoDto.getUserId(): "+sessionInfoDto.getUserId());

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        List<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeListMain(0);
        modelAndView.addObject("traceInfoList", defaultQueryDtoInterface);

        if(sessionInfoDto == null) {
            modelAndView.setViewName("redirect:/user/login");
        } else {
            modelAndView.addObject("sessionInfo", sessionInfoDto);
        }


        return modelAndView;
    }

    @GetMapping("/index")
    public ModelAndView index2(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/index");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        log.info("sessionInfoDto.getUserId(): "+sessionInfoDto.getUserId());

        List<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getNoticeListMain(0);
        modelAndView.addObject("traceInfoList", defaultQueryDtoInterface);

        return modelAndView;
    }

    @GetMapping("/password")
    public ModelAndView password(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/password");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

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

        if(manageType.equals("아이디")) {
            modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserIdContainingOrderByUserUnoDesc("99", "Y", keyword, pageRequest));
        } else if(manageType.equals("이름")) {
            modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserNmContainingOrderByUserUnoDesc("99", "Y", keyword, pageRequest));
        } else if(manageType.equals("전체")) {
            modelAndView.addObject("counselorInfoList", userRepository.findAllByEntire("99", "Y", keyword, pageRequest));
//            modelAndView.addObject("counselorInfoList", userRepository.findAllByUserClfCdNotAndUseYnAndUserIdContainingOrUserNmContainingOrderByUserUnoDesc("99", "Y", keyword, keyword, pageRequest));
        } else {
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

    // 카운트
    @PostMapping("/history_tsi_uno_count")
    public int history_tsi_uno_count(@RequestParam(required = false) Integer tsi_uno){
        int tsiUnoCount = searchJobRepository.countByTsiUno(tsi_uno);
        return tsiUnoCount;
    }

    // 신조어 카운트
    @PostMapping("/newKeyword_tsi_uno_count")
    public int newKeyword_tsi_uno_count(@RequestParam(required = false) Integer tsi_uno){
        int tsiUnoCount = searchJobRepository.countByTsiUno(tsi_uno);
        return tsiUnoCount;
    }
/*
    @GetMapping("/history")
    public ModelAndView history(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword) {     // 검색 이력, 추적 이력

        ModelAndView modelAndView = new ModelAndView("html/history");
        Map<String, Object> searchHistMap = new HashMap<>();
        Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            // searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            searchHistMapCnt = searchService.getSearchInfoResultCnt("10","0", searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            // searchHistMapCnt = searchService.getSearchInfoResultCnt(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");
        // 추적이력
        Map<String, Object> traceHistoryMap = searchService.getTraceHistoryList(tracePage, traceKeyword);

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchInfoList", searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchHistMapCnt",searchHistMapCnt);

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }
    */

    @GetMapping("/history")
    public ModelAndView history(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                @RequestParam(required = false, defaultValue = "") String userKeyword,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                @RequestParam(required=false, defaultValue = "0") String traceHistoryValue) {
        ModelAndView modelAndView = new ModelAndView("html/history");
        Map<String, Object> searchHistMap = null;

        log.info("userKeyword: "+userKeyword +" traceKeyword: "+traceKeyword);
        // Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);

            // searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            // searchHistMapCnt = searchService.getSearchInfoResultCnt("10","0", searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            // searchHistMapCnt = searchService.getSearchInfoResultCnt(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");
        Map<String, Object> traceHistoryMap = null;
        // 추적이력
        if(traceHistoryValue.equals("0")){
            traceHistoryMap = searchService.getTraceHistoryList(tracePage, traceKeyword);
        } else if(traceHistoryValue.equals("10")){
            traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword);
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        assert searchHistMap != null;
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/result")
    public ModelAndView result(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                               @RequestParam(value = "tsiUno") Optional<Integer> tsiUno,
                               @RequestParam(value = "tsiKeyword") Optional<String> tsiKeyword,
                               @RequestParam(required = false, defaultValue = "1") Integer page,
                               @RequestParam(required = false, defaultValue = "") String keyword,
                               @RequestParam(required = false, defaultValue = "list") String listType,
                               @RequestParam(required = false, defaultValue = "") String tsjStatusAll,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus1,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus2,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus3,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus4,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus11,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus01,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus00,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus10,
                               @RequestParam(required = false, defaultValue = "") String odStatusAll,
                               @RequestParam(required = false, defaultValue = "") String odStatus01,
                               @RequestParam(required = false, defaultValue = "") String odStatus02,
                               @RequestParam(required = false, defaultValue = "") String odStatus03,
                               @RequestParam(required = false, defaultValue = "") String snsStatusAll,
                               @RequestParam(required = false, defaultValue = "") String snsStatus01,
                               @RequestParam(required = false, defaultValue = "") String snsStatus02,
                               @RequestParam(required = false, defaultValue = "") String snsStatus03,
                               @RequestParam(required = false, defaultValue = "") String snsStatus04,
                               @RequestParam(required = false, defaultValue = "1") String priority) {
        ModelAndView modelAndView = new ModelAndView("html/result");
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = null;

        log.debug("priority => {}", priority);
        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        int histTsiUno = tsiUno.get();

        searchService.searchResultHistInsert(userUno, userId, histTsiUno);

        //검색 조건 값이 다 없을 경우
        if (!StringUtils.hasText(tsjStatusAll)
                && !StringUtils.hasText(tsjStatus00) // 대기중
                && !StringUtils.hasText(tsjStatus01) // 처리중
                && !StringUtils.hasText(tsjStatus10) // SKIP
                && !StringUtils.hasText(tsjStatus11)) { // 일치율
            tsjStatusAll = "1";
        }

        if (!StringUtils.hasText(odStatusAll)
                && !StringUtils.hasText(odStatus01)
                && !StringUtils.hasText(odStatus02)
                && !StringUtils.hasText(odStatus03)) {
            odStatusAll = "1";
        }

        if (!StringUtils.hasText(snsStatusAll)
                && !StringUtils.hasText(snsStatus01)
                && !StringUtils.hasText(snsStatus02)
                && !StringUtils.hasText(snsStatus03)
                && !StringUtils.hasText(snsStatus04)) {
            snsStatusAll = "1";
        }

        //2023-03-22 값이 없어서 추가
        modelAndView.addObject("tsjStatusAll", tsjStatusAll); // 분류
        modelAndView.addObject("odStatusAll", odStatusAll);   // 일치율 높은순
        modelAndView.addObject("snsStatusAll", snsStatusAll); // SNS

        if(tsiUno.isPresent()) {
            modelAndView.addObject("tsiUno", tsiUno.get());
            modelAndView.addObject("imgSrc", searchService.getSearchInfoImgUrl(tsiUno.get()));
            modelAndView.addObject("tsiType", searchService.getSearchInfoTsiType(tsiUno.get()));

            //tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4 값이 안넘어와서 세팅 추가
            if ("1".equals(tsjStatus11)) {
                tsjStatus1 = "11";
            }
            if ("1".equals(tsjStatus01)) {
                tsjStatus2 = "01";
            }
            if ("1".equals(tsjStatus00)) {
                tsjStatus3 = "00";
            }
            if ("1".equals(tsjStatus10)) {
                tsjStatus4 = "10";
            }

            if ("1".equals(snsStatus01)) {
                snsStatus01 = "11";
            }
            if ("1".equals(snsStatus02)) {
                snsStatus02 = "13";
            }
            if ("1".equals(snsStatus03)) {
                snsStatus03 = "15";
            }
            if ("1".equals(snsStatus04)) {
                snsStatus04 = "17";
            }

            modelAndView.addObject("tsjStatus11", tsjStatus11);//일치율
            modelAndView.addObject("tsjStatus01", tsjStatus01);//처리중
            modelAndView.addObject("tsjStatus00", tsjStatus00);//대기중
            modelAndView.addObject("tsjStatus10", tsjStatus10);//SKIP

            modelAndView.addObject("odStatus01", odStatus01);//이미지
            modelAndView.addObject("odStatus02", odStatus02);//오디오
            modelAndView.addObject("odStatus03", odStatus03);//텍스트

            modelAndView.addObject("snsStatus01", snsStatus01);//구글
            modelAndView.addObject("snsStatus02", snsStatus02);//트위터
            modelAndView.addObject("snsStatus03", snsStatus03);//인스타
            modelAndView.addObject("snsStatus04", snsStatus04);//페이스북

            if(!"".equals(tsjStatusAll)) {
                tsjStatus1 = "00";
                tsjStatus2 = "01";
                tsjStatus3 = "10";
                tsjStatus4 = "11";
            }

            if(!"".equals(odStatusAll)) {
                odStatus01 = "1";
                odStatus02 = "1";
                odStatus03 = "1";
            }

            if(!"".equals(snsStatusAll)) {
                snsStatus01 = "11";
                snsStatus02 = "13";
                snsStatus03 = "15";
                snsStatus04 = "17";
            }

            String order_type = "";

            if("1".equals(odStatus01) && "1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "0"; // 모두선택 또는 전체
            }else if("1".equals(odStatus01) && !"1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "1"; // 이미지만선택
            }else if(!"1".equals(odStatus01) && "1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "2"; // 오디오만선택
            }else if(!"1".equals(odStatus01) && !"1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "3"; // 텍스트만선택
            }else if("1".equals(odStatus01) && "1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "4"; //이미지, 오디오 선택
            }else if("1".equals(odStatus01) && !"1".equals(odStatus02) && "1".equals(odStatus03)){
                order_type = "5"; //이미지, 텍스트 선택
            }
            /*else if(!"1".equals(odStatus01) && "1".equals(odStatus02) && !"1".equals(odStatus03)){
                order_type = "6"; //오디오, 텍스트 선택
            }*/

            defaultQueryDtoInterface = searchService.getSearchResultList(tsiUno.get(), keyword, page, priority, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, order_type);

        }
        tsiKeyword.ifPresent(s -> modelAndView.addObject("tsiKeyword", s));
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultList", defaultQueryDtoInterface);
        assert defaultQueryDtoInterface != null;
        modelAndView.addObject("searchResultListCount", defaultQueryDtoInterface.getTotalElements());
        modelAndView.addObject("number", defaultQueryDtoInterface.getNumber());
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);
        modelAndView.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());
        modelAndView.addObject("listType", listType);
        modelAndView.addObject("keyword", keyword);
        modelAndView.addObject("userId", searchService.getUserIdByTsiUnoMap().get(tsiUno.get()));

        return modelAndView;
    }
    @GetMapping("/notice")
    public ModelAndView notice(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , @RequestParam(required = false, defaultValue = "0", value = "tsiUno") Integer tsiUno
            , @RequestParam(required = false,  defaultValue = "0", value = "tsiKeyword") String tsiKeyword
            , @RequestParam(required = false, defaultValue = "80") String tsjStatus
            , @RequestParam (required = false, defaultValue = "0") Integer tsrUno
            , @RequestParam(required = false, defaultValue = "1") Integer page) {
        int percent = sessionInfoDto.getPercent_limit();
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = null;

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.noticeHistInsert(userUno, userId);

        // defaultQueryDtoInterface = searchService.getNoticeList(page,tsiuno, percent, keyword, tsiKeyword);
        defaultQueryDtoInterface = searchService.getNoticeList(page, tsiUno, percent, tsiKeyword);

        ModelAndView modelAndView = new ModelAndView("html/notice");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultList", defaultQueryDtoInterface);
        modelAndView.addObject("tsrUno", tsrUno);
        assert defaultQueryDtoInterface != null;
        modelAndView.addObject("searchResultListCount", defaultQueryDtoInterface.getTotalElements());
        modelAndView.addObject("number", defaultQueryDtoInterface.getNumber());
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);
        modelAndView.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());

        modelAndView.addObject("tsrUno", tsrUno);
        modelAndView.addObject("tsiUno", tsiUno);
        modelAndView.addObject("imgSrc", searchService.getSearchInfoImgUrl(tsiUno)); //tsi
        modelAndView.addObject("tsiType", searchService.getSearchInfoTsiType(tsiUno)); //tsi
        modelAndView.addObject("userId", searchService.getUserIdByTsiUnoMap().get(tsiUno)); //tsi
        modelAndView.addObject("tsiKeyword", tsiKeyword);

        return modelAndView;
    }

    /*
    @GetMapping("/notice")
    public ModelAndView notice(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , @RequestParam(required = false, defaultValue = "0", value = "tsiUno") Optional<Integer> tsiUno
            , @RequestParam(required = false, defaultValue = "80") String tsjStatus
            , @RequestParam (required = false, defaultValue = "0") Integer tsrUno
            , @RequestParam (required = false, defaultValue = "0") Integer tsiuno
            , @RequestParam(required = false, defaultValue = "1") Integer page
            , @RequestParam(required = false, defaultValue = "") String keyword
            , @RequestParam(required = false, defaultValue = "") String tsiKeyword) {

        log.info("tsiuno: "+tsiuno);
        log.info("tsiUno: " + tsiUno);
        log.info("---------------------------");
        log.info("keyword: " + keyword);
        log.info("tsiKeyword: "+tsiKeyword);

        int percent = sessionInfoDto.getPercent_limit();
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = null;

        log.info("page:"+page);
        log.info("===========================");
        defaultQueryDtoInterface = searchService.getNoticeList(page,tsiuno, percent, keyword, tsiKeyword);

        ModelAndView modelAndView = new ModelAndView("html/notice");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultList", defaultQueryDtoInterface);
        modelAndView.addObject("tsrUno", tsrUno);
        assert defaultQueryDtoInterface != null;
        modelAndView.addObject("searchResultListCount", defaultQueryDtoInterface.getTotalElements());
        modelAndView.addObject("number", defaultQueryDtoInterface.getNumber());
        modelAndView.addObject("maxPage", Consts.MAX_PAGE);
        modelAndView.addObject("totalPages", defaultQueryDtoInterface.getTotalPages());

        modelAndView.addObject("tsrUno", tsrUno);
        modelAndView.addObject("tsiUno", tsiuno);
        modelAndView.addObject("imgSrc", searchService.getSearchInfoImgUrl(tsiuno)); //tsi
        modelAndView.addObject("tsiType", searchService.getSearchInfoTsiType(tsiuno)); //tsi
        modelAndView.addObject("userId", searchService.getUserIdByTsiUnoMap().get(tsiuno)); //tsi
        modelAndView.addObject("keyword", keyword);

//        searchService.getNotice(tsjStatus, tsrUno, page, modelAndView);

        return modelAndView;
    }

*/
    @GetMapping("/result-detail")
    public ModelAndView result_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                      @RequestParam Integer tsiUno) {

        ModelAndView modelAndView = new ModelAndView("html/result-detail");
        log.info("tsiUno: "+tsiUno);
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        // modelAndView.addObject("searchResultInfo", searchService.getResultInfo(tsiUno));
        modelAndView.addObject("searchResultInfo", searchService.getInfoList(tsiUno));
        log.info("searchService.getInfoList(tsiUno): " + searchService.getInfoList(tsiUno));

        return modelAndView;
    }

    @GetMapping("/info")
    public ModelAndView info(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/info");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    @GetMapping("/loading")
    public ModelAndView loading(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/loading");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    @GetMapping("/trace")
    public ModelAndView trace(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                              @RequestParam(required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = "") String trkStatCd,
                              @RequestParam(required = false, defaultValue = "") String keyword,
                              @RequestParam(required = false, defaultValue = "list") String listType) {
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

    @GetMapping("/keyword") // 여기
    public ModelAndView trace_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/keyword");
        log.info("========= keyword 페이지 진입 ========");
        // Map<String, Object> autoKeyword_list = userService.getAutoKeyword(sessionInfoDto.getUserId());
        // List<NewKeywordEntity> newKeywordList = searchService.getNewKeywordList();
        List<NewKeywordDto> newKeywordList = newKeywordRepository.keywordList();

        // modelAndView.addObject("autoKeyword_list", autoKeyword_list.get("autoKeyword_list"));
        modelAndView.addObject("newKeywordList", newKeywordList);
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
    public String deleteTsrUno(HttpServletResponse response, Integer tsrUno) {
        log.info("tsrUno: " + tsrUno);

        searchService.deleteTsrUno(tsrUno);

        return "success";
    }


    @GetMapping("/deleteTsrUnos")
    public String deleteTsrUnos(HttpServletResponse response,
                                @RequestParam(value="tsrUnoValues", required=false) List<Integer> tsrUnoValues) {
        log.info("tsrUnoValues: "+tsrUnoValues);
        searchService.deleteTsrUnos(tsrUnoValues);

        return "success";
    }

    @GetMapping("/monitoringList")
    public ModelAndView monitoringList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                       @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                       @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                       @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                       @RequestParam(required = false, defaultValue = "") String traceKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/history2");
        log.info("monitoringList 진입");
        Map<String, Object> searchHistMap = null;
        // Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);

            // searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            // searchHistMapCnt = searchService.getSearchInfoResultCnt("10","0", searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            // searchHistMapCnt = searchService.getSearchInfoResultCnt(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");
        // 추적이력
        Map<String, Object> traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword);

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        assert searchHistMap != null;
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/monitoringDeleteReqList")
    public ModelAndView monitoringDeleteReqList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                       @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                       @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                       @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                       @RequestParam(required = false, defaultValue = "") String traceKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/history3");
        log.info("monitoringList 진입");
        Map<String, Object> searchHistMap = null;
        // Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);

            // searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            // searchHistMapCnt = searchService.getSearchInfoResultCnt("10","0", searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            // searchHistMapCnt = searchService.getSearchInfoResultCnt(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");
        // 추적이력
        Map<String, Object> traceHistoryMap = searchService.getTraceHistoryDeleteReqList(tracePage, traceKeyword);

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        assert searchHistMap != null;
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/monitoringDeleteComptList")
    public ModelAndView monitoringDeleteComptList(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                                @RequestParam(required = false, defaultValue = "") String traceKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/history4");
        log.info("monitoringList 진입");
        Map<String, Object> searchHistMap = null;
        // Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        log.info("traceKeyword: "+traceKeyword);
        log.info("searchKeyword: "+searchKeyword);

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);

            // searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
            // searchHistMapCnt = searchService.getSearchInfoResultCnt("10","0", searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            // searchHistMapCnt = searchService.getSearchInfoResultCnt(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");
        // 추적이력
        Map<String, Object> traceHistoryMap = searchService.getTraceHistoryDeleteComptList(tracePage, traceKeyword);

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        assert searchHistMap != null;
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

        return modelAndView;
    }

    @GetMapping("/userSearchHistory")
    public ModelAndView userSearchHistory(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                          @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                          @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/userSearchHistory");
        log.info("searchKeyword: "+searchKeyword+" page: " + searchPage);
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "userSearchHistory");

        Map<String, Object> userSearchHistoryList = new HashMap<>();

       // userSearchHistoryList = searchService.getUserSearchHistoryList(searchPage, searchKeyword);

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("userSearchHistoryList", userSearchHistoryList.get("userSearchHistoryList"));
        modelAndView.addObject("userSearchHistoryListCount", userSearchHistoryList.get("totalElements"));
        modelAndView.addObject("searchNumber", userSearchHistoryList.get("number"));
        modelAndView.addObject("maxPage", userSearchHistoryList.get("maxPage"));
        modelAndView.addObject("searchTotalPages", userSearchHistoryList.get("totalPages"));
        // modelAndView.addObject("searchKeyword", searchKeyword);


        return modelAndView;
    }

    @GetMapping("/allTimeMonitoringHist")
    public ModelAndView allTimeMonitoringHist(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                                @RequestParam String tsrUno) {
        log.info(" === allTimeMonitoringHist 진입 === " + tsrUno);
        ModelAndView modelAndView = new ModelAndView("html/allTimeMonitoringHist");

        List<AllTimeMonitoringHistDto> allTimeMonitoringHistList = alltimeMonitoringHistRepository.allTimeMonitoringList(String.valueOf(tsrUno));

        for(int i=0; i<allTimeMonitoringHistList.size(); i++){
            log.info("amh: "+allTimeMonitoringHistList.get(i).getClkDmlDt());
        }

        modelAndView.addObject("allTimeMonitoringHistList", allTimeMonitoringHistList);
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }


}
