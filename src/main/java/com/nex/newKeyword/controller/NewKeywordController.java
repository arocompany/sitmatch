package com.nex.newKeyword.controller;

import com.nex.common.Consts;
import com.nex.newKeyword.service.NewKeywordService;
import com.nex.search.entity.NewKeywordEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.repo.NewKeywordRepository;
import com.nex.search.service.*;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SearchKeywordDto;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/keyword")
public class NewKeywordController {
    private final SearchService searchService;
    private final NewKeywordService newKeywordService;
    private final NewKeywordRepository newKeywordRepository;

    // 신조어 이력관리
    @GetMapping("/newKeyword")
    public ModelAndView newKeyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , @RequestParam(required = false, defaultValue = "1") Integer searchPage
            , @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/newKeyword");
        log.info("========= keyword 페이지 진입 ========");

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "newKeyword");

        Map<String, Object> newKeywordInfoList = new HashMap<>();

        log.info("history sessionInfoDto: " + sessionInfoDto.getUserId());
        /*
        if(sessionInfoDto.isAdmin()) {
            log.info("newKeyword 검색이력 진입");
            newKeywordInfoList = searchService.getNewKeywordInfoList(searchPage, searchKeyword);
        } else {
            newKeywordInfoList = searchService.getNewKeywordInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }
        */
        if(sessionInfoDto.isAdmin()) {
            log.info("newKeyword 검색이력 진입");
            newKeywordInfoList = newKeywordService.getNewKeywordInfoList(searchPage, searchKeyword);
            // newKeywordInfoList = searchService.getSearchInfoList(searchPage, searchKeyword);
        } else {
            newKeywordInfoList = newKeywordService.getNewKeywordInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
            //newKeywordInfoList = searchService.getSearchInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap());
        modelAndView.addObject("newKeywordInfoList", newKeywordInfoList.get("newKeywordInfoList"));
        modelAndView.addObject("newKeywordInfoListCount", newKeywordInfoList.get("totalElements"));
        modelAndView.addObject("searchNumber", newKeywordInfoList.get("number"));
        modelAndView.addObject("maxPage", newKeywordInfoList.get("maxPage"));
        modelAndView.addObject("searchTotalPages", newKeywordInfoList.get("totalPages"));
        modelAndView.addObject("searchKeyword", searchKeyword);

        return modelAndView;
    }

    @PostMapping("/newKeyword")
    public Boolean newKeyword(@RequestParam(value="newKeywordValues", required=false) List<String> newKeywordValues,
                              @SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                              ) throws ExecutionException, InterruptedException {
        log.info("newKeywordValues3: " + newKeywordValues);
        // ModelAndView modelAndView = new ModelAndView("redirect:/search/newKeyword");
        List<SearchKeywordDto> searchKeywordList  = newKeywordRepository.searchKeywordList();

        for(int i=0; i<searchKeywordList.size(); i++) {
            log.info(" newKeyword for문 진입 ");
            SearchInfoEntity searchInfoEntity = new SearchInfoEntity();
            String newKeyword = searchKeywordList.get(i).getKeyword();
            log.info("newKeyword: " +newKeyword );
            String tsiType="11";

            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String folder = now.format(formatter);
            searchInfoEntity.setTsiType("11");
            searchInfoEntity.setTsiGoogle((byte) 1);
            searchInfoEntity.setTsiTwitter((byte) 1);
            searchInfoEntity.setTsiFacebook((byte) 1);
            searchInfoEntity.setTsiInstagram((byte) 1);
            searchInfoEntity.setUserUno(sessionInfoDto.getUserUno());
            searchInfoEntity.setTsiStat("11");
            searchInfoEntity.setTsiKeyword(newKeyword);

            SearchInfoEntity insertResult = newKeywordService.saveNewKeywordSearchInfo(searchInfoEntity);
            newKeywordService.searchByText(newKeyword, insertResult, folder);
        }

/*
        for(int i=0; i< nrd.size(); i++) {
            SearchInfoEntity searchInfoEntity = new SearchInfoEntity();
            log.info("newKeyword: "+nrd.get(i).toString());
            String newKeyword = nrd.get(i).toString();

            String tsiType="11";

            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            String folder = now.format(formatter);
            searchInfoEntity.setTsiType("11");
            searchInfoEntity.setTsiGoogle((byte) 1);
            searchInfoEntity.setTsiTwitter((byte) 1);
            searchInfoEntity.setTsiFacebook((byte) 1);
            searchInfoEntity.setTsiInstagram((byte) 1);
            searchInfoEntity.setUserUno(sessionInfoDto.getUserUno());
            searchInfoEntity.setTsiStat("11");
            searchInfoEntity.setTsiKeyword(newKeyword);

            SearchInfoEntity insertResult = newKeywordService.saveNewKeywordSearchInfo(searchInfoEntity);
            newKeywordService.searchByText(newKeyword, insertResult, folder);

        }

        log.info("========= newKeyword 검색 완료 =========");
*/
        return true;

    }

    @PostMapping("/add_keyword")
    public List<NewKeywordDto> add_keyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , String keyword)  {
        log.info("keyword: "+keyword);

        newKeywordService.addNewKeyword(sessionInfoDto, keyword);
        return newKeywordRepository.keywordList();
    }

    @GetMapping("/del_keyword")
    @ResponseBody
    public List<NewKeywordDto> del_keyword(Integer idx) {
        log.info("idx: " + idx);

        NewKeywordEntity nke = newKeywordRepository.findByIdx(idx);
        nke.setKeywordStus("1");
        newKeywordRepository.save(nke);
        return newKeywordRepository.keywordList();
    }


}
