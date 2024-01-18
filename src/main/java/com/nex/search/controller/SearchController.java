package com.nex.search.controller;

import com.nex.common.Consts;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    @PostMapping()
    public ModelAndView search(@RequestParam("file") Optional<MultipartFile> file, SearchInfoEntity searchInfoEntity
                                ,@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                                ,SearchInfoDto searchInfoDto, Model model) throws Exception {
        ModelAndView mv = new ModelAndView("redirect:/history");

        if(searchInfoEntity.getTsiKeyword() == null){
            mv = new ModelAndView("redirect:/");
            return mv;
        }

        log.info("search진입 tsiKeyword {}", searchInfoEntity.getTsiKeyword());
        // String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String folder = now.format(formatter);

        searchInfoEntity.setUserUno(sessionInfoDto.getUserUno());
        searchInfoEntity.setTsiStat("11");
        SearchInfoEntity resultEntity = searchService.insertSearchInfo(file.get(), searchInfoEntity, folder);

        if(resultEntity != null) {
            searchService.search(resultEntity, searchInfoDto, folder);
        }else{
            mv = new ModelAndView("redirect:/");
            return mv;
        }


        return mv;
    }

    @GetMapping("/deleteTsiUnos")
    public String deleteSearchInfo(@RequestParam(value="tsiUnosValue", required=false) List<Integer> tsiUnosValue) {
        log.info("tsiUnos: "+tsiUnosValue);
        searchService.deleteTsiUnos(tsiUnosValue);
        return "success";
    }


    @GetMapping("deleteSearchInfo")
    public String deleteSearchInfo(@RequestParam Optional<Integer> tsiUno) {
        tsiUno.ifPresent(searchService::deleteSearchInfo);
        return "success";
    }

    @GetMapping("deleteMornitoringInfo")
    public String deleteMornitoringInfo(@RequestParam Optional<Integer> tsrUno) {
        tsrUno.ifPresent(searchService::deleteMornitoringInfo);
        return "success";
    }

    @GetMapping("addTrkStat")
    public String addTrkStat(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                             @RequestParam Optional<Integer> tsrUno) {
        if(tsrUno.isPresent()){
            int userUno = sessionInfoDto.getUserUno();
            String userId = sessionInfoDto.getUserId();
            searchService.addTrkStat(userUno,userId, tsrUno.get());
        }
        // tsrUno.ifPresent(searchService::addTrkStat);
        return "success";
    }

    @GetMapping("setTrkHistMemo")
    public String setTrkHistMemo(@RequestParam Optional<Integer> tsrUno,
                                 @RequestParam(required = false, defaultValue = "") String memo) {
        tsrUno.ifPresent(integer -> searchService.setTrkHistMemo(integer, memo));
        return "success";
    }

    @GetMapping("setTrkStatCd")
    public String setTrkStatCd(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                               @RequestParam Optional<Integer> tsrUno,
                               @RequestParam String trkStatCd) {
        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        log.info(" setTrkStatCd 진입: "+trkStatCd);

        if(trkStatCd.equals("20")) { // 삭제요청
            tsrUno.ifPresent(integer -> searchService.setTrkStatCd(userUno, userId, integer, "20"));
        } else if(trkStatCd.equals("30")) { // 삭제완료
            tsrUno.ifPresent(integer -> searchService.setTrkStatCd(userUno, userId, integer, "30"));
        } else {
            tsrUno.ifPresent(integer -> searchService.setTrkStatCd(userUno, userId, integer, trkStatCd));
        }

        return "success";
    }

    @GetMapping("/monitoring")
    public String setMonitoringCd(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                  @RequestParam Optional<Integer> tsrUno) {

        log.info(" === setMonitoringCd 진입 여기 === ");
        int userUno = sessionInfoDto.getUserUno();
        String userId = sessionInfoDto.getUserId();
        // log.info("tsrUno: "+ tsrUno.get() + " traceTsiUno: "+traceTsiUno.get());
        // monitoring_cd= 10 비활성화  // 20 활성화
        searchService.setMonitoringCd(userUno, userId, tsrUno.get());
        // tsrUno.ifPresent(searchService::setMonitoringCd);
        return "success";
    }
}

