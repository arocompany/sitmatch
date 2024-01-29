package com.nex.base.controller;

import com.nex.common.Consts;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class HistoryController {
    private final SearchService searchService;

    @GetMapping("/history")
    public ModelAndView history(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                @RequestParam(required = false, defaultValue = "1") Integer searchPage,
                                @RequestParam(required = false, defaultValue = "") String searchKeyword,
                                @RequestParam(required = false, defaultValue = "1") Integer tracePage,
                                @RequestParam(required = false, defaultValue = "") String traceUserKeyword,
                                @RequestParam(required = false, defaultValue = "") String traceKeyword,
                                @RequestParam(required=false, defaultValue = "0") String traceHistoryValue,
                                @RequestParam(required = false, defaultValue = "검색어") String manageType) {
        ModelAndView modelAndView = new ModelAndView("html/history");
        Map<String, Object> searchHistMap;

        log.info(" == history 진입 == manageType: " + manageType);
        modelAndView.addObject("manageType", manageType);

        if (manageType.equals("검색어")) {
            manageType = "1";
        } else {
            manageType = "2";
        }

        log.info("traceUserKeyword: " + traceUserKeyword + " traceKeyword: " + traceKeyword);
        // Page<List<ResultCntQueryDtoInterface>> searchHistMapCnt = null;

        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        searchService.searchInfoHistInsert(userUno, userId, searchKeyword, traceKeyword);

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "history");

        if(sessionInfoDto.isAdmin()) {
            log.info("검색이력 진입");
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            searchHistMap = searchService.getSearchInfoList(searchPage, searchKeyword, userUno);
        }

        // int Percent = sessionInfoDto.getPercent_limit();
        log.info("추적이력 진입");

        // 추적이력
        Map<String, Object> traceHistoryMap = null;

        // 검색어(타이틀) 검색
        if(manageType.equals("1")) {
            if(traceHistoryValue.equals("0")){
                traceHistoryMap = searchService.getTraceHistoryList(tracePage, traceKeyword);
            } else if(traceHistoryValue.equals("10")){
                traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword);
            }
        } else { // 대상자 검색
            if(traceHistoryValue.equals("0")){
                traceHistoryMap = searchService.getTraceHistoryUserFileList(tracePage, traceKeyword);
            } else if(traceHistoryValue.equals("10")){
                traceHistoryMap = searchService.getTraceHistoryMonitoringList(tracePage, traceKeyword);
            }
        }

        // 검색이력 데이터
        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("searchKeyword", searchKeyword);
        modelAndView.addObject("searchInfoList",searchHistMap.get("searchInfoList"));
        modelAndView.addObject("searchInfoListCount", searchHistMap.get("totalElements"));
        modelAndView.addObject("searchNumber", searchHistMap.get("number"));
        modelAndView.addObject("maxPage", searchHistMap.get("maxPage"));
        modelAndView.addObject("searchTotalPages", searchHistMap.get("totalPages"));

        // 추적이력 데이터
        modelAndView.addObject("traceHistoryList", Objects.requireNonNull(traceHistoryMap).get("traceHistoryList"));

        modelAndView.addObject("traceNumber", traceHistoryMap.get("number"));
        modelAndView.addObject("maxPage", traceHistoryMap.get("maxPage"));
        modelAndView.addObject("traceTotalPages", traceHistoryMap.get("totalPages"));
        modelAndView.addObject("traceKeyword", traceKeyword);


        modelAndView.addObject("traceHistoryListCount", searchService.getResultByTrace());
        modelAndView.addObject("countMonitoring", traceHistoryMap.get("countMonitoring")); // 모니터링
        modelAndView.addObject("countDelReq", traceHistoryMap.get("countDelReq"));         // 삭제요청
        modelAndView.addObject("countDelCmpl", traceHistoryMap.get("countDelCmpl"));       // 삭제완료
        modelAndView.addObject("allTimeMonitoringCnt", traceHistoryMap.get("allTimeMonitoringCnt")); // 24시간 모니터링

        modelAndView.addObject("tsiTypeMap", searchService.getTsiTypeMap());
//        modelAndView.addObject("tsiKeyword", searchService.getTsiKeywordMap());
//        modelAndView.addObject("tsiFstDmlDt", searchService.getTsiFstDmlDtMap());

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

        if(tsiUno.isPresent()){
            int histTsiUno = tsiUno.get();
            searchService.searchResultHistInsert(userUno, userId, histTsiUno);
        }

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
}
