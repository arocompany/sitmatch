package com.nex.search.controller;

import com.nex.common.Consts;
import com.nex.search.ImageService.*;
import com.nex.search.entity.NationCodeEntity;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.dto.SearchInfoDto;
import com.nex.search.repo.NationCodeRepository;
import com.nex.search.service.SearchService;
import com.nex.search.textFacebookService.*;
import com.nex.search.textGoogleService.*;
import com.nex.search.textImageFacebookService.*;
import com.nex.search.textImageGoogleService.*;
import com.nex.search.textImageInstagramService.*;
import com.nex.search.textInstagramService.*;
import com.nex.user.entity.SessionInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController // JSON 형태 결과값을 반환해줌 (@ResponseBody가 필요없음)
@RequiredArgsConstructor // final 객체를 Constructor Injection 해줌. (Autowired 역할)
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;
    private final SearchImageGoogleLensService searchImageGoogleLensService;

    private final SearchTextGoogleService searchTextGoogleService;
    private final SearchTextInstagramService searchTextInstagramService;
    private final SearchTextFacebookService searchTextFacebookService;

    private final SearchImageService searchImageService;
    private final SearchTextImageGoogleService searchTextImageGoogleService;
    private final SearchTextImageInstagramService searchTextImageInstagramService;
    private final SearchTextImageFacebookService searchTextImageFacebookService;



    @Value("${search.yandex.text.url}")
    private String textYandexUrl;
    @Value("${search.yandex.text.gl}")
    private String textYandexGl;
    @Value("${search.yandex.text.no_cache}")
    private String textYandexNocache;
    @Value("${search.yandex.text.location}")
    private String textYandexLocation;
    @Value("${search.yandex.text.tbm}")
    private String textYandexTbm;
    @Value("${search.yandex.text.api_key}")
    private String textYandexApikey;
    @Value("${search.yandex.text.engine}")
    private String textYandexEngine;
    @Value("${search.yandex.image.engine}")
    private String imageYandexEngine;
    @Value("${search.yandex.text.count.limit}")
    private String textYandexCountLimit;
    @Value("${file.location1}")
    private String fileLocation1;
    @Value("${file.location3}")
    private String fileLocation3;
    @Value("${server.url}")
    private String serverIp;
    private String searchImageUrl;
    private Boolean loop = true;
    private final NationCodeRepository nationCodeRepository;


    @PostMapping("")
    public ModelAndView search(@RequestParam("file") Optional<MultipartFile> file, SearchInfoEntity searchInfoEntity
                                ,@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto
                                ,SearchInfoDto searchInfoDto) throws Exception {
        ModelAndView modelAndView = new ModelAndView("redirect:/history");

        log.info("search진입 tsiKeyword "+searchInfoEntity.getTsiKeyword());
        // String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();
        String tsiKeyword = searchInfoEntity.getTsiKeyword();
        String tsiType;
        byte tsiGoogle = searchInfoEntity.getTsiGoogle();
        byte tsiFacebook = searchInfoEntity.getTsiFacebook();
        // byte tsiTwitter = searchInfoEntity.getTsiTwitter();
        byte tsiInstagram = searchInfoEntity.getTsiInstagram();
        boolean isFile = !file.get().isEmpty();

        searchInfoEntity.setUserUno(sessionInfoDto.getUserUno());
        searchInfoEntity.setTsiStat("11");
        SearchInfoEntity insertResult;

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String folder = now.format(formatter);

        if(isFile){ // 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            try{
                InputStream inputStream = file.get().getInputStream();
                Tika tika = new Tika();
                String mimeType = tika.detect(inputStream);
                if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("video")){// 비디오
                    searchInfoEntity.setTsiImgHeight("");
                    searchInfoEntity.setTsiImgWidth("");
                    searchInfoEntity.setTsiImgSize(String.valueOf(file.get().getSize() / 1024));
                    searchInfoEntity.setSearchValue("0");
                    if(tsiKeyword.isEmpty()){
                        tsiType = "19";
                        searchInfoEntity.setTsiType(tsiType);
                    } else {
                        tsiType = "15";
                        searchInfoEntity.setTsiType(tsiType);
                    }
                } else if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("image")){// 이미지 업로드
                    BufferedImage bi = ImageIO.read(file.get().getInputStream());
                    searchInfoEntity.setTsiImgHeight(String.valueOf(bi.getHeight()));
                    searchInfoEntity.setTsiImgWidth(String.valueOf(bi.getWidth()));
                    searchInfoEntity.setTsiImgSize(String.valueOf(file.get().getSize() / 1024));
                    bi.flush();
                    if(tsiKeyword.isEmpty()){
                        tsiType = "17";
                        searchInfoEntity.setTsiType(tsiType);
                    } else {
                        tsiType = "13";
                        searchInfoEntity.setTsiType(tsiType);
                    }
                } else {// 그 외 파일 업로드
                    searchInfoEntity.setTsiImgHeight("");
                    searchInfoEntity.setTsiImgWidth("");
                    searchInfoEntity.setTsiImgSize("");
                    modelAndView = new ModelAndView("redirect:/");
                    return modelAndView;
                }

                String origName = file.get().getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String extension = origName.substring(origName.lastIndexOf("."));

                String filePath = fileLocation1+folder;
                File destdir = new File(filePath);
                if(!destdir.exists()){
                    destdir.mkdirs();
                }

                file.get().transferTo(new File(destdir+File.separator+uuid+extension));

                searchInfoEntity.setTsiImgName(uuid+extension);
                searchInfoEntity.setTsiImgPath((destdir+File.separator).replaceAll("\\\\", "/"));
                searchInfoEntity.setTsiImgExt(extension.substring(1));

                if(tsiKeyword.isEmpty()){
                    searchImageUrl = serverIp + fileLocation3+"/"+folder+"/" + uuid+extension;
                }

            }catch(Exception e){
                e.printStackTrace();
                log.error(e.getMessage());
                modelAndView = new ModelAndView("redirect:/");
                return modelAndView;
            }

        } else {
            tsiType = "11";
            searchInfoEntity.setTsiType(tsiType);
        }

        insertResult = searchService.saveSearchInfo(searchInfoEntity);

        // 검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지

        switch (tsiType) {
            case "11" -> { // 11:키워드
                if (tsiGoogle == 1) {
                    searchService.searchYandexYoutube("11", insertResult, searchInfoDto);
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextGoogleService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }
                if (tsiInstagram == 1) {
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextInstagramService.search(tsiInstagram, tsiType, insertResult, folder, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }
                if (tsiFacebook == 1) {
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextFacebookService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }
            }
            case "13" -> { // 13:키워드+이미지
                searchImageGoogleLensService.searchYandexByGoogleLensImage("11", insertResult);
                if (tsiGoogle == 1) {
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextImageGoogleService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }

                if (tsiInstagram == 1) {
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextImageInstagramService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }
                if (tsiFacebook == 1) {
                    List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                    for(NationCodeEntity ncInfo : ncList) {
                        searchTextImageFacebookService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                    }
                }
            }
            case "15" -> // 15:키워드+영상
                    searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiType, insertResult, folder, searchInfoDto);
            case "17" -> { // 17:이미지
                log.info("== case17 진입 ==");
                // searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder, searchInfoDto);

                searchImageGoogleLensService.searchYandexByGoogleLensImage("11", insertResult);

                List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
                for(NationCodeEntity ncInfo : ncList) {
                    searchImageService.search(insertResult, searchInfoDto, ncInfo.getNcCode().toLowerCase());
                }
                /*
                searchImageCnService.search(insertResult, searchInfoDto);
                searchImageKrService.search(insertResult, searchInfoDto);
                searchImageNlService.search(insertResult, searchInfoDto);
                searchImageRuService.search(insertResult, searchInfoDto);
                searchImageThService.search(insertResult, searchInfoDto);
                searchImageUsService.search(insertResult, searchInfoDto);
                searchImageVnService.search(insertResult, searchInfoDto);
                */
            }
            case "19" -> // 19: 영상
                    searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiType, insertResult, folder, searchInfoDto);
        }

       // searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder, searchInfoDto);

        log.info("====== search 끝 ======");

        return modelAndView;
    }


    @Deprecated
    private void searchGoogle(String tsiType, SearchInfoEntity insertResult, String folder, String tsrSns) throws Exception {
        // Google 검색기능 구현 (yandex 검색 (텍스트, 텍스트+사진, 이미지검색-구글 렌즈), 구글 검색(텍스트))
        switch (tsiType) {// 검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지
            case "11":// 키워드만 검색한 경우
                // Yandex 검색
                log.info("키워드 검색");
                searchYandexByText(tsrSns, insertResult);
                // Google Custom Search 검색
//                    searchGoogleCustomByText(insertResult);
                break;
            case "13"://키워드 + 이미지 검색인 경우
                // Yandex 검색
                log.info("키워드/이미지 검색");
                searchYandexByText(tsrSns, insertResult);
                // Google Custom Search 검색
//                    searchGoogleCustomByText(insertResult);
                break;
            case "15"://키워드 + 영상 검색인 경우
                // 영상처리
                // Yandex 검색
                log.info("키워드/영상 검색");
                searchYandexByText(tsrSns, insertResult);
                break;
            case "17"://이미지만 검색인 경우
                // Yandex 검색
                log.info("이미지 검색");
                searchYandexByImage(tsrSns, insertResult);
                break;
            case "19"://영상만 검색인 경우
                // Yandex 검색
                log.info("영상 검색");
                searchService.searchYandexByVideo(tsrSns, insertResult, fileLocation3, folder);
                break;
        }
    }

    @Deprecated
    public void searchYandexByText(String tsrSns, SearchInfoEntity insertResult){
        int index = 0;
        String tsiKeyword = insertResult.getTsiKeyword();

        //인스타
        if ("15".equals(tsrSns)) {
            tsiKeyword = "인스타그램 " + tsiKeyword;
        }
        //페북
        else if ("17".equals(tsrSns)) {
            tsiKeyword = "페이스북 " + tsiKeyword;
        }

        do {
            // yandex search url
            String url = textYandexUrl
                    + "?q=" + tsiKeyword
                    + "&gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&tbm=" + textYandexTbm
                    + "&ijn=" + String.valueOf(index)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=" + textYandexEngine;

            CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            // text기반 yandex 검색 및 결과 저장.(이미지)
                            return searchService.searchYandexByText(url, tsrSns, insertResult);
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                            return null;
                        }
                    })
                    .thenAccept((r) -> {
                        try {
                            // yandex검색을 통해 결과 db에 적재.
                            searchService.saveImgSearchYandexByText(r, insertResult);
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                        }
                    });
            // properties에 설정된 limit 에 따라 총 데이터 저장 한계를 설정하는 부분
            // ex)limit이 10이라면 페이지당 데이터가 100건이므로 100건*limit 10 해서
            // 검색 결과가 limit 건수보다 많다면 총 1000건의 데이터까지만 저장,
            // 해당 검색 결과가 limit 건수보다 낮다면 데이터 건수만큼만 저장됨
            if(index >= Integer.parseInt(textYandexCountLimit)-1){
                loop = false;
            }

            index++;
        } while (loop);
    }

    // Yandex 이미지 검색 후처리
    public void searchYandexByImage(String tsrSns, SearchInfoEntity insertResult){
        String url = textYandexUrl
                + "&gl=" + textYandexGl
                + "&no_cache=" + textYandexNocache
                + "&api_key=" + textYandexApikey
                + "&engine=" + imageYandexEngine
                + "&safe=off"
                + "&filter=0"
                + "&nfpr=0"
                + "&image_url=" + searchImageUrl;

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색 및 결과 저장.(이미지)
                        return searchService.searchYandexByImage(url, tsrSns, insertResult);
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                        return null;
                    }
                })
                .thenApplyAsync((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        return searchService.saveImgSearchYandexByImage(r, insertResult);
                    } catch (Exception e) {
                        log.debug(e.getMessage());
                        return null;
                    }
                });
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

/*
    @PostMapping("/newKeyword")
    public Boolean newKeyword(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                              @RequestParam(value="newKeywordValues", required=false) List<String> newKeywordValues) throws ExecutionException, InterruptedException {
        log.info("newKeywordValues3: " + newKeywordValues);
        // ModelAndView modelAndView = new ModelAndView("redirect:/search/newKeyword");

        List<String> nrd = newKeywordValues;


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

            SearchInfoEntity insertResult = searchService.saveNewKeywordSearchInfo(searchInfoEntity);
            searchService.searchByText(newKeyword, insertResult, folder);

        }

        log.info("========= newKeyword 검색 완료 =========");

        return true;

    }

 */

}

