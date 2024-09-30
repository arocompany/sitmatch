package com.nex.newKeyword.controller;

import com.nex.common.Consts;
import com.nex.newKeyword.service.NewKeywordService;
import com.nex.search.entity.NewKeywordEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.repo.NewKeywordRepository;
import com.nex.search.service.SearchService;
import com.nex.user.entity.NewKeywordDto;
import com.nex.user.entity.SearchKeywordDto;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    @GetMapping("/")
    public ModelAndView keyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("html/keyword");
        List<NewKeywordDto> newKeywordList = newKeywordRepository.keywordList();
        modelAndView.addObject("newKeywordList", newKeywordList);
        modelAndView.addObject("sessionInfo", sessionInfoDto);

        return modelAndView;
    }

    // 신조어 이력관리
    @GetMapping("/newKeyword")
    public ModelAndView newKeyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                                , @RequestParam(required = false, defaultValue = "1") Integer searchPage
                                , @RequestParam(required = false, defaultValue = "") String searchKeyword) {
        ModelAndView modelAndView = new ModelAndView("html/newKeyword");

        modelAndView.addObject("sessionInfo", sessionInfoDto);
        modelAndView.addObject("headerMenu", "newKeyword");

        Map<String, Object> newKeywordInfoList = null;

        if(sessionInfoDto.isAdmin()) {
            newKeywordInfoList = newKeywordService.getNewKeywordInfoList(searchPage, searchKeyword);
        } else {
            newKeywordInfoList = newKeywordService.getNewKeywordInfoList(searchPage, searchKeyword, sessionInfoDto.getUserUno());
        }

        List<SearchInfoEntity> list = ((Page<SearchInfoEntity>)newKeywordInfoList.get("newKeywordInfoList")).getContent();
        List<Integer> tsiUnoList = list.stream().map(SearchInfoEntity::getTsiUno).toList();

        modelAndView.addObject("userCount", searchService.getUserIdMap());
        modelAndView.addObject("userIdMap", searchService.getUserIdMap());
        modelAndView.addObject("getProgressPercentMap", searchService.getProgressPercentMap(tsiUnoList));
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
        List<SearchKeywordDto> searchKeywordList  = newKeywordRepository.searchKeywordList();

        for(int i=0; i<searchKeywordList.size(); i++) {
            SearchInfoEntity searchInfoEntity = new SearchInfoEntity();
            SearchInfoDto searchInfoDto = new SearchInfoDto();
            String newKeyword = searchKeywordList.get(i).getKeyword();

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
            searchInfoEntity.setTsufUno(0);
            searchInfoEntity.setTsiKeyword(newKeyword);

            SearchInfoEntity insertResult = newKeywordService.saveNewKeywordSearchInfo(searchInfoEntity);
            searchInfoDto.setTsiKeywordHiddenValue(newKeyword);

            if(insertResult != null){ searchService.saveSearchInfoParams(insertResult); }

            searchService.search(insertResult, searchInfoDto, folder);
        }
        return true;
    }

    @PostMapping("/add_keyword")
    public List<NewKeywordDto> add_keyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
            , String keyword)  {
        newKeywordService.addNewKeyword(sessionInfoDto, keyword);
        return newKeywordRepository.keywordList();
    }

    @GetMapping("/del_keyword")
    @ResponseBody
    public List<NewKeywordDto> del_keyword(Integer idx) {
        NewKeywordEntity nke = newKeywordRepository.findByIdx(idx);
        nke.setKeywordStus("1");
        newKeywordRepository.save(nke);
        return newKeywordRepository.keywordList();
    }
}
