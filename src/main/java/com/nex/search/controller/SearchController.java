package com.nex.search.controller;

import com.nex.common.Consts;
import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.service.SearchService;
import com.nex.user.entity.SessionInfoDto;
import com.nex.user.entity.UserEntity;
import com.nex.user.repo.UserRepository;
import com.nex.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.tika.Tika;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.oauth2.login.OAuth2LoginSecurityMarker;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.attribute.UserPrincipal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController // JSON 형태 결과값을 반환해줌 (@ResponseBody가 필요없음)
@RequiredArgsConstructor // final 객체를 Constructor Injection 해줌. (Autowired 역할)
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

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


    @PostMapping("")
    public ModelAndView search(@RequestParam("file") Optional<MultipartFile> file, SearchInfoEntity searchInfoEntity, HttpServletRequest request
                                ,@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto) {
        ModelAndView modelAndView = new ModelAndView("redirect:/history");

        String tsiKeyword = searchInfoEntity.getTsiKeyword();
        String tsiType = "";
        byte tsiGoogle = searchInfoEntity.getTsiGoogle();
        byte tsiFacebook = searchInfoEntity.getTsiFacebook();
        byte tsiTwitter = searchInfoEntity.getTsiTwitter();
        byte tsiInstagram = searchInfoEntity.getTsiInstagram();
        boolean isFile = !file.get().isEmpty();

        // 미현주석
        System.out.println("유저 번호 " + sessionInfoDto.getUserUno());
        //searchInfoEntity.setUserUno(1);
        searchInfoEntity.setUserUno(sessionInfoDto.getUserUno());
        searchInfoEntity.setTsiStat("11");
        SearchInfoEntity insertResult =  new SearchInfoEntity();

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String folder = now.format(formatter);

        if(isFile){

            try{
                InputStream inputStream = file.get().getInputStream();
                Tika tika = new Tika();
                String mimeType = tika.detect(inputStream);
                if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("video")){// 비디오 업로드
                    searchInfoEntity.setTsiImgHeight("");
                    searchInfoEntity.setTsiImgWidth("");
                    searchInfoEntity.setTsiImgSize(String.valueOf(file.get().getSize() / 1024));
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

        /*
        if(tsiGoogle == 1){
            // Google 검색기능 구현
            String tsrSns = "11";

            // Google 검색기능 구현 (yandex 검색 (텍스트, 텍스트+사진, 이미지검색-구글 렌즈), 구글 검색(텍스트))
            searchGoogle(tsiType, insertResult, folder, tsrSns);
        }

        //2023-03-20
        //Facebook, Instagram 도 Google 로 검색, 링크로 Facebook, Instagram 판별
        if(tsiFacebook == 1){
            // Facebook 검색기능 구현
            String tsrSns = "17";

            // Google 검색기능 구현 (yandex 검색 (텍스트, 텍스트+사진, 이미지검색-구글 렌즈), 구글 검색(텍스트))
            searchGoogle(tsiType, insertResult, folder, tsrSns);
        }

        //2023-03-20
        //Facebook, Instagram 도 Google 로 검색, 링크로 Facebook, Instagram 판별
        if(tsiInstagram == 1){
            // Instagram 검색기능 구현
            String tsrSns = "15";

            // Google 검색기능 구현 (yandex 검색 (텍스트, 텍스트+사진, 이미지검색-구글 렌즈), 구글 검색(텍스트))
            searchGoogle(tsiType, insertResult, folder, tsrSns);
        }

        if(tsiTwitter == 1){
            // Twitter 검색기능 구현
        }
         */
        //2023-03-26 위 로직 searchService 로 이동
        searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder);

        return modelAndView;
    }

    /**
     * @Deprecated 2023-03-26 사용 중지 SearchService 로 이동 {@link SearchService#searchGoogle(String, SearchInfoEntity, String, String)}
     */
    @Deprecated
    private void searchGoogle(String tsiType, SearchInfoEntity insertResult, String folder, String tsrSns) {
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

    /**
     * @Deprecated 2023-03-26 사용 중지 SearchService 로 이동 {@link SearchService#searchYandexByText(String, SearchInfoEntity)}}
     */
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
                    + "&GL=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&tbm=" + textYandexTbm
                    + "&ijn=" + String.valueOf(index)
                    + "&api_key=" + textYandexApikey
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
                + "?GL=" + textYandexGl
                + "&no_cache=" + textYandexNocache
                + "&api_key=" + textYandexApikey
                + "&engine=" + imageYandexEngine
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

    @GetMapping("deleteSearchInfo")
    public String deleteSearchInfo(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                   @RequestParam Optional<Integer> tsiUno) {
        tsiUno.ifPresent(searchService::deleteSearchInfo);
        return "success";
    }

    @GetMapping("deleteMornitoringInfo")
    public String deleteMornitoringInfo(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                        @RequestParam Optional<Integer> tsrUno) {
        tsrUno.ifPresent(searchService::deleteMornitoringInfo);
        return "success";
    }

    @GetMapping("addTrkStat")
    public String addTrkStat(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                             @RequestParam Optional<Integer> tsrUno) {
        tsrUno.ifPresent(searchService::addTrkStat);
        return "success";
    }

    @GetMapping("setTrkHistMemo")
    public String setTrkHistMemo(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                 @RequestParam Optional<Integer> tsrUno,
                                 @RequestParam(required = false, defaultValue = "") String memo) {
        tsrUno.ifPresent(integer -> searchService.setTrkHistMemo(integer, memo));
        return "success";
    }

    @GetMapping("setTrkStatCd")
    public String setTrkStatCd(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                               @RequestParam Optional<Integer> tsrUno,
                               @RequestParam String trkStatCd) {
        tsrUno.ifPresent(integer -> searchService.setTrkStatCd(integer, trkStatCd));
        return "success";
    }

    @GetMapping("/monitoring")
    public String setMonitoringCd(@SessionAttribute(name = Consts.LOGIN_SESSION, required = false) SessionInfoDto sessionInfoDto,
                                  @RequestParam Optional<Integer> tsrUno) {
        tsrUno.ifPresent(searchService::setMonitoringCd);
        return "success";
    }

}

