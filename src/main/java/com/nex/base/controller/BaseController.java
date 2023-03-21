package com.nex.base.controller;

import com.nex.common.Consts;
import com.nex.search.entity.DefaultQueryDtoInterface;
import com.nex.search.service.SearchService;
import com.nex.user.entity.AutoKeywordInterface;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.repo.UserRepository;
import com.nex.user.repo.AutoRepository;
import com.nex.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class BaseController {

    private final UserRepository userRepository;
    private final SearchService searchService;
    private final UserService userService;
    private final AutoRepository autoRepository;

    @GetMapping("/")
    public ModelAndView index(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/index");
        modelAndView.addObject("headerMenu", "index");
        log.debug("sessionInfoDto = {}", sessionInfoDto);
        if(sessionInfoDto == null) {
            modelAndView.setViewName("redirect:/user/login");
        } else {
            modelAndView.addObject("sessionInfo", sessionInfoDto);
        }

        List<DefaultQueryDtoInterface> defaultQueryDtoInterface = searchService.getTraceListByHome();
        modelAndView.addObject("traceInfoList", defaultQueryDtoInterface);

        return modelAndView;
    }

    @GetMapping("/index")
    public ModelAndView index2(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/index");
        modelAndView.addObject("sessionInfo", sessionInfoDto);

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

    @GetMapping("/notice")
    public ModelAndView notice(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/notice");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
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

    @GetMapping("/history")
    public ModelAndView history(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword) {     // 검색 이력, 추적 이력
        ModelAndView modelAndView = new ModelAndView("html/history");
        Map<String, Object> searchHistMap = new HashMap<>();

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        Map<String, Object> traceHistoryMap = searchService.getTraceHistoryList(tracePage, traceKeyword);

        // 검색이력 데이터
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchInfoList", searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));
        modelAndView.addObject("searchKeyword", searchKeyword);

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", traceHistoryMap.get("traceHistoryList"));
//        modelAndView.addObject("traceHistoryList", searchService2.getTraceHistoryList(tracePage, traceKeyword).get("traceHistoryList"));
        modelAndView.addObject("traceHistoryListCount", traceHistoryMap.get("totalElements"));
        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);

        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring"));
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));

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
                               @RequestParam(required = false, defaultValue = "1") String tsjStatusAll,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus1,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus2,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus3,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus4,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus11,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus01,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus00,
                               @RequestParam(required = false, defaultValue = "") String tsjStatus10,
                               @RequestParam(required = false, defaultValue = "1") String priority) {
        ModelAndView modelAndView = new ModelAndView("html/result");
        Page<DefaultQueryDtoInterface> defaultQueryDtoInterface = null;

        log.debug("priority => {}", priority);

//        String order_by_0 = "";
//        String order_by_1 = " FIELD(TSJ_STATUS, ";

//        if(tsjStatus11.equals("1")){
//            order_by_0 += " TMR_SIMILARITY ASC ";
//        }
//
//        order_by_1 = " , "+order_by_1;
//
//        if(tsjStatus01.equals("1")){
//            if(order_by_1.equals(" FIELD(TSJ_STATUS, ")){
//                order_by_1 += " '01'";
//            }else{
//                order_by_1 += " ,'01'";
//            }
//        }
//
//        if(tsjStatus00.equals("1")){
//            if(order_by_1.equals(" FIELD(TSJ_STATUS, ")){
//                order_by_1 += " '00'";
//            }else{
//                order_by_1 += " ,'00'";
//            }
//        }
//
//        if(tsjStatus10.equals("1")){
//            if(order_by_1.equals(" FIELD(TSJ_STATUS, ")){
//                order_by_1 += " '11'";
//            }else{
//                order_by_1 += " ,'11'";
//            }
//        }
//
//        if(order_by_1.equals(" FIELD(TSJ_STATUS, ")) {
//            order_by_1 = "";
//        }
//
//        if(!order_by_0.equals("") && !order_by_1.equals("")){
//            order_by_2 = order_by_0+", "+order_by_1;
//        }
//
//        if(!order_by_2.equals("")){
//            order_by_2 += " ) asc, ";
//        }



        modelAndView.addObject("tsjStatus11", tsjStatus11);//일치율
        modelAndView.addObject("tsjStatus01", tsjStatus01);//처리중
        modelAndView.addObject("tsjStatus00", tsjStatus00);//대기중
        modelAndView.addObject("tsjStatus10", tsjStatus10);//SKIP

        if(tsiUno.isPresent()) {
            modelAndView.addObject("tsiUno", tsiUno.get());
            modelAndView.addObject("imgSrc", searchService.getSearchInfoImgUrl(tsiUno.get()));
            modelAndView.addObject("tsiType", searchService.getSearchInfoTsiType(tsiUno.get()));
            defaultQueryDtoInterface = searchService.getSearchResultList(tsiUno.get(), keyword, page, priority, tsjStatusAll, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4);
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

    @GetMapping("/result-detail")
    public ModelAndView result_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                      @RequestParam Integer tsrUno) {
        ModelAndView modelAndView = new ModelAndView("html/result-detail");
        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("searchResultInfo", searchService.getResultInfo(tsrUno));

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

    @GetMapping("/keyword")
    public ModelAndView trace_detail(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/keyword");
        Map<String, Object> autoKeyword_list = userService.getAutoKeyword(sessionInfoDto.getUserId());

        modelAndView.addObject("autoKeyword_list", autoKeyword_list.get("autoKeyword_list"));
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

}
