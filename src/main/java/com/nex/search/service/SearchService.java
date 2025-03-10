package com.nex.search.service;

import com.nex.Chart.entity.*;
import com.nex.Chart.repo.*;
import com.nex.common.CommonCode;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.Consts;
import com.nex.common.SitProperties;
import com.nex.nations.entity.NationCodeEntity;
import com.nex.nations.repository.NationCodeRepository;
import com.nex.search.entity.*;
import com.nex.search.entity.dto.*;
import com.nex.search.repo.*;
import com.nex.serpServices.entity.SerpServicesEntity;
import com.nex.serpServices.repo.SerpServicesRepository;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Configuration
@RequiredArgsConstructor
public class SearchService {
    private final SearchImageService searchImageService;
    private final SearchImageGoogleLensService searchImageGoogleLensService;
    private final SearchTextService searchTextService;
    private final SearchTextServiceForGoogleImage searchTextServiceForGoogleImage;

    private final SearchVideoService searchVideoService;
    private final SearchVideoYandexService searchVideoYandexService;
    private final SearchYoutubeService searchYoutubeService;

    private final SearchTextBaiduService searchTextBaiduService;
    private final SearchTextBingService searchTextBingService;
    private final SearchTextDuckduckgoService searchTextDuckduckgoService;
    private final SearchTextYahooService searchTextYahooService;
    private final SearchTextYandexService searchTextYandexService;
    private final SearchImageYandexService searchImageYandexService;
    private final SearchTextNaverService searchTextNaverService;
    private final SearchVideoGoogleLensService searchVideoGoogleLensService;

    private final NationCodeRepository nationCodeRepository;
    private final SerpServicesRepository serpServicesRepository;

    private final SearchInfoRepository searchInfoRepository;
    private final SearchResultRepository searchResultRepository;
    private final VideoInfoRepository videoInfoRepository;
    private final SearchJobRepository searchJobRepository;
    private final MonitoringHistRepository monitoringHistRepository;
    private final DeleteReqHistRepository deleteReqHistRepository;
    private final DeleteComptHistRepository deleteComptHistRepository;
    private final AlltimeMonitoringHistRepository alltimeMonitoringHistRepository;

    private final SearchInfoHistRepository searchInfoHistRepository;
    private final TraceHistRepository traceHistRepository;
    private final SearchResultHistRepository searchResultHistRepository;
    private final NoticeHistRepository noticeHistRepository;
    private final SearchInfoParamsRepository searchInfoParamsRepository;

    private final SitProperties sitProperties;
    private final SearchUserFileRepository searchUserFileRepository;

    public SearchInfoEntity insertSearchInfo(MultipartFile uploadFile, SearchInfoEntity param, String folder, SearchInfoDto sDto){
        boolean isFile = ! uploadFile.isEmpty();

        if(isFile){ // 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            try{
                InputStream inputStream = uploadFile.getInputStream();
                Tika tika = new Tika();
                String mimeType = tika.detect(inputStream);

                log.error("123123===" + mimeType);
                if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("video") || mimeType.indexOf("octet-stream") > -1){// 비디오
                    param.setTsiImgHeight("");
                    param.setTsiImgWidth("");
                    param.setTsiImgSize(String.valueOf(uploadFile.getSize() / 1024));
                    param.setSearchValue("0");
                    if(! StringUtils.hasText(param.getTsiKeyword())){
                        param.setTsiType("19");
                    } else {
                        param.setTsiType("15");
                    }
                } else if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("image")){// 이미지 업로드
                    BufferedImage bi = ImageIO.read(uploadFile.getInputStream());

                    // TODO : image/webp mime type에 대한 후 처리 또는 예외 처리 필요
                    if(bi != null) {
                        param.setTsiImgHeight(String.valueOf(bi.getHeight()));
                        param.setTsiImgWidth(String.valueOf(bi.getWidth()));
                        param.setTsiImgSize(String.valueOf(uploadFile.getSize() / 1024));
                        bi.flush();
                    }

                    if(! StringUtils.hasText(param.getTsiKeyword())){
                        param.setTsiType("17");
                    } else {
                        param.setTsiType("13");
                    }
                } else {
                    if(!StringUtils.hasText(sDto.getTsiKeywordHiddenValue())){
                        return null;
                    }
                }

                String origName = uploadFile.getOriginalFilename();
                int idx = origName.lastIndexOf(".");
                String userFile =origName.substring(0, idx);

                String uuid = UUID.randomUUID().toString();
                String extension = origName.substring(origName.lastIndexOf(".")).toLowerCase();

                String filePath = sitProperties.getFileLocation1()+folder;
                File destDir = new File(filePath);
                if(!destDir.exists()){
                    destDir.mkdirs();
                }

                uploadFile.transferTo(new File(destDir+File.separator+uuid+extension));

                {
                    param.setTsiUserFile(userFile);


                }
                param.setTsiImgName(uuid+extension);
                param.setTsiImgPath((destDir+File.separator).replaceAll("\\\\", "/"));
                param.setTsiImgExt(extension.substring(1));
            }catch(Exception e){
                e.printStackTrace();
                log.error(e.getMessage());
                return null;
            }
        } else {
            param.setTsiType("11");
        }

        try{
            if(StringUtils.hasText(param.getTsiUserFile())){
                SearchUserFileEntity searchUserFileEntity = searchUserFileRepository.findByTsufUserFile(param.getTsiUserFile());
                if(searchUserFileEntity == null){
                    searchUserFileEntity = new SearchUserFileEntity();
                    searchUserFileEntity.setTsufUserFile(param.getTsiUserFile());
                }
                searchUserFileEntity.setLastDmlDt(Timestamp.valueOf(LocalDateTime.now()));
                searchUserFileEntity = searchUserFileRepository.save(searchUserFileEntity);
                if(searchUserFileEntity != null) param.setTsufUno(searchUserFileEntity.getTsufUno());
            }
        }catch (Exception e){
            e.printStackTrace();
        }



        return saveSearchInfo(param);
    }
    public void search(SearchInfoEntity param, SearchInfoDto siDto, String folder){
        try {
            // 활성화된 언어 리스트
            List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
            // 활성화된 검색엔진 리스트
            List<SerpServicesEntity> ssList = serpServicesRepository.findBySsIsActive(1);

            List<String> files = null;
            if(param.getTsiType().equals(CommonCode.searchTypeVideo) || param.getTsiType().equals(CommonCode.searchTypeKeywordVideo)){
                String DATA_DIRECTORY = param.getTsiImgPath() + param.getTsiUno() + "/";
                File dir = new File(DATA_DIRECTORY);
                if(! dir.exists()){
                    dir.mkdirs();
                }
                try {
                    files = searchVideoService.processVideo(param);
                }catch(Exception e){
                    log.error(e.getMessage());
                }
            }

            int cntNation = 0;
            for (NationCodeEntity ncInfo : ncList) {
                switch (param.getTsiType()) {
                    // 11:키워드
                    case CommonCode.searchTypeKeyword -> {
                        for(SerpServicesEntity ssInfo : ssList) {
                            switch (ssInfo.getSsName()){
                                case CommonCode.SerpAPIEngineGoogle -> {
                                    if (param.getTsiGoogle() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }

                                    if (param.getTsiGoogle() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineYoutube -> {
                                    if (param.getTsiGoogle() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeGoogle, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiInstagram() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeInstagram, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiFacebook() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeFacebook, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiTwitter() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeTwitter, param, siDto, ncInfo.getNcCode().toLowerCase());
                                }
                                case CommonCode.SerpAPIEngineBaidu -> {
                                    if(ncInfo.getNcCode().equals("cn")) {
                                        if (param.getTsiGoogle() == 1){ searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineBing -> {
                                    if(! ncInfo.getNcCode().equals("cn") && ! ncInfo.getNcCode().equals("th") && ! ncInfo.getNcCode().equals("ru") && ! ncInfo.getNcCode().equals("vn")) {
                                        if (param.getTsiGoogle() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineDuckduckgo -> {
                                    if (param.getTsiGoogle() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineYahoo -> {
                                    if (param.getTsiGoogle() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
//                                case CommonCode.SerpAPIEngineYandex -> {
//                                    if(! ncInfo.getNcCode().equals("vn")) {
//                                        if (param.getTsiGoogle() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
//                                        if (param.getTsiInstagram() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
//                                        if (param.getTsiFacebook() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
//                                        if (param.getTsiTwitter() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
//                                    }
//                                }
                                case CommonCode.SerpAPIEngineNaver -> {
                                    if(ncInfo.getNcCode().equals("kr")) {
                                        if (param.getTsiGoogle() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                            }
                        }
                    }
                    // 13:키워드+이미지
                    case CommonCode.searchTypeKeywordImage -> {
                        for(SerpServicesEntity ssInfo : ssList) {
                            switch (ssInfo.getSsName()){
                                case CommonCode.SerpAPIEngineGoogleReverseImage -> {
                                    if (param.getTsiGoogle() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1) { searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }

                                    if (param.getTsiGoogle() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }

//                                    searchImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase());
//                                    if (param.getTsiGoogle() == 1) { searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
//                                    if (param.getTsiInstagram() == 1) { searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
//                                    if (param.getTsiFacebook() == 1) { searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
//                                    if (param.getTsiTwitter() == 1) { searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineGoogleLens -> {
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "vmimage");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "emimage");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "about-this-image");
                                }
//                                case CommonCode.SerpAPIEngineYandexImage -> {
//                                    if(! ncInfo.getNcCode().equals("vn")) {
//                                        searchImageYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase());
//                                        if (param.getTsiGoogle() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
//                                        if (param.getTsiInstagram() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
//                                        if (param.getTsiFacebook() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
//                                        if (param.getTsiTwitter() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
//                                    }
//                                }

                                /* 키워드 검색엔진 부분 */
                                case CommonCode.SerpAPIEngineYoutube -> {
                                    if (param.getTsiGoogle() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeGoogle, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiInstagram() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeInstagram, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiFacebook() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeFacebook, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiTwitter() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeTwitter, param, siDto, ncInfo.getNcCode().toLowerCase());
                                }
                                case CommonCode.SerpAPIEngineBaidu -> {
                                    if(cntNation == 0) {
                                        if (param.getTsiGoogle() == 1){ searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineBing -> {
                                    if(! ncInfo.getNcCode().equals("cn") && ! ncInfo.getNcCode().equals("th") && ! ncInfo.getNcCode().equals("ru") && ! ncInfo.getNcCode().equals("vn")) {
                                        if (param.getTsiGoogle() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineDuckduckgo -> {
                                    if (param.getTsiGoogle() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineYahoo -> {
                                    if (param.getTsiGoogle() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineNaver -> {
                                    if(cntNation == 0) {
                                        if (param.getTsiGoogle() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                            }
                        }
                    }
                    // 15:키워드+영상
                    case CommonCode.searchTypeKeywordVideo -> {
                        for(SerpServicesEntity ssInfo : ssList) {
                            switch (ssInfo.getSsName()){
                                case CommonCode.SerpAPIEngineGoogle -> {
//                                    if (param.getTsiGoogle() == 1) { searchVideoService.searchByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files);}
//                                    if (param.getTsiFacebook() == 1) { searchVideoService.searchByTextVideo(CommonCode.snsTypeFacebook, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files); }
//                                    if (param.getTsiInstagram() == 1) { searchVideoService.searchByTextVideo(CommonCode.snsTypeInstagram, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files); }
//                                    if (param.getTsiTwitter() == 1) { searchVideoService.searchByTextVideo(CommonCode.snsTypeTwitter, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files); }

                                    if (param.getTsiGoogle() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1) { searchTextServiceForGoogleImage.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineGoogleLens -> {
                                    searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "");
                                    searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "vmimage");
                                    searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "emimage");
                                    searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "about-this-image");
                                }
//                                case CommonCode.SerpAPIEngineYandex -> {
//                                    if(! ncInfo.getNcCode().equals("vn")) {
//                                        searchVideoYandexService.searchByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files);
//                                        if (param.getTsiGoogle() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
//                                        if (param.getTsiInstagram() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
//                                        if (param.getTsiFacebook() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
//                                        if (param.getTsiTwitter() == 1){searchTextYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
//                                    }
//                                }

                                /* 키워드 검색엔진 부분 */
                                case CommonCode.SerpAPIEngineYoutube -> {
                                    if (param.getTsiGoogle() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeGoogle, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiInstagram() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeInstagram, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiFacebook() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeFacebook, param, siDto, ncInfo.getNcCode().toLowerCase());
                                    if (param.getTsiTwitter() == 1)  searchYoutubeService.searchYoutube(CommonCode.snsTypeTwitter, param, siDto, ncInfo.getNcCode().toLowerCase());
                                }
                                case CommonCode.SerpAPIEngineBaidu -> {
                                    if(cntNation == 0) {
                                        if (param.getTsiGoogle() == 1){ searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1){searchTextBaiduService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineBing -> {
                                    if(! ncInfo.getNcCode().equals("cn") && ! ncInfo.getNcCode().equals("th") && ! ncInfo.getNcCode().equals("ru") && ! ncInfo.getNcCode().equals("vn")) {
                                        if (param.getTsiGoogle() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextBingService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                                case CommonCode.SerpAPIEngineDuckduckgo -> {
                                    if (param.getTsiGoogle() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextDuckduckgoService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineYahoo -> {
                                    if (param.getTsiGoogle() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                    if (param.getTsiInstagram() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                    if (param.getTsiFacebook() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                    if (param.getTsiTwitter() == 1){searchTextYahooService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                }
                                case CommonCode.SerpAPIEngineNaver -> {
                                    if(cntNation == 0) {
                                        if (param.getTsiGoogle() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle); }
                                        if (param.getTsiInstagram() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram); }
                                        if (param.getTsiFacebook() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook); }
                                        if (param.getTsiTwitter() == 1) { searchTextNaverService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeTwitter); }
                                    }
                                }
                            }
                        }
                    }
                    // 17:이미지
                    case CommonCode.searchTypeImage -> {
                        for(SerpServicesEntity ssInfo : ssList) {
                            switch (ssInfo.getSsName()){
//                                case CommonCode.SerpAPIEngineGoogleReverseImage -> searchImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase());
                                case CommonCode.SerpAPIEngineGoogleLens -> {
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "vmimage");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "emimage");
                                    searchImageGoogleLensService.searchByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase(), "about-this-image");
                                }
//                                case CommonCode.SerpAPIEngineYandexImage -> {
//                                    if( !ncInfo.getNcCode().equals("vn") ){searchImageYandexService.search(param, siDto, ncInfo.getNcCode().toLowerCase());}
//                                }
                            }
                        }
                    }
                    // 19: 영상
                    case CommonCode.searchTypeVideo -> {
                        if(files != null) {
                            for (SerpServicesEntity ssInfo : ssList) {
                                switch (ssInfo.getSsName()) {
//                                    case CommonCode.SerpAPIEngineGoogleReverseImage -> searchVideoService.searchByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files);
                                    case CommonCode.SerpAPIEngineGoogleLens -> {
                                        searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "");
                                        searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "vmimage");
                                        searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "emimage");
                                        searchVideoGoogleLensService.searchByGoogleLensVideo(CommonCode.snsTypeGoogle, param, folder, ncInfo.getNcCode().toLowerCase(), files, "about-this-image");
                                    }
//                                    case CommonCode.SerpAPIEngineYandexImage -> {
//                                        if (!ncInfo.getNcCode().equals("vn")) { searchVideoYandexService.searchByTextVideo(CommonCode.snsTypeGoogle, param, siDto, folder, ncInfo.getNcCode().toLowerCase(), files);
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                }
                cntNation++;
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

//    /**
//     * 텍스트, 이미지 검색
//     *
//     * {@link #searchByText(String, String, SearchInfoEntity)} {@link #searchByImage(String, String, SearchInfoEntity)}}
//     *
//     * @param url         (URL)
//     * @param infoClass   (YandexByTextResult or YandexByImageResult Class)
//     * @param getErrorFn  (info error getter Function)
//     * @param getResultFn (RESULT getter Function)
//     * @param <INFO>      (YandexByTextResult or YandexByImageResult)
//     * @param <RESULT>    (Images_resultsByText or Images_resultsByImage)
//     * @return List<RESULT> (RESULT List)
//     * @throws Exception
//     */

    public SearchInfoEntity saveSearchInfo(SearchInfoEntity sie) {
        CommonStaticSearchUtil.setSearchInfoDefault(sie);
        return searchInfoRepository.save(sie);
    }

//    /**
//     * 검색 결과 엔티티 기본값 세팅
//     *
//     * @param sre (검색 결과 엔티티)
//     */
    public Page<DefaultQueryDtoInterface> getSearchResultList(Integer tsiUno, String keyword, Integer page, String priority,
                                                              String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                              String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String isImage, String order_type, List<String> nationCode) {
        PageRequest pageRequest = PageRequest.of(page - 1, 10);

        log.debug("priority => {}", priority);

        // String orderByTmrSimilarityDesc = " ORDER BY tmrSimilarity desc, TMR.TSR_UNO desc";

        if ("1".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_1");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_1(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else if ("2".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_2");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_2(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else if ("3".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_3");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_3(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else if ("4".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_4");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_4(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else if ("5".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_5");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_5(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else if ("6".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_6");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_6(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        } else {
            log.info("getResultInfoListOrderByTmrSimilarityDesc");
            log.info("pageRequest" + pageRequest);
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, isImage, nationCode, pageRequest);
        }
    }

    public Page<DefaultQueryDtoInterface> getNoticeList(Integer page, Integer tsiUno, String tsiKeyword, Integer tsiSearchType) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        log.info("pageRequest: " + pageRequest);
        log.info("tsiuno: " + tsiUno);

        if (tsiUno == 0) {
            return searchResultRepository.getNoticeList(tsiSearchType, pageRequest);
        } else {
            return StringUtils.hasText(tsiKeyword) ? searchResultRepository.getNoticeSelList(pageRequest, tsiUno, tsiKeyword, tsiSearchType) : searchResultRepository.getNoticeSelListEmptyKeyword(pageRequest, tsiUno, tsiSearchType);
        }
    }

    public List<DefaultQueryDtoInterface> getNoticeListMain(Integer percent) {
        return searchResultRepository.getNoticeListMain(percent, 0);
    }

    public DefaultQueryDtoInterface getResultInfo(Integer tsrUno) {
        return searchResultRepository.getResultInfo(tsrUno);
    }

    public DefaultQueryDtoInterface getSearchInfo(Integer tsiUno) {
        return searchResultRepository.getInfoList(tsiUno);
    }


    public Page<DefaultQueryDtoInterface> getTraceList(Integer page, String trkStatCd, String keyword, Integer tsiSearchType) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        if (trkStatCd.equals("삭제 요청 중")) {
            trkStatCd = "20";
        } else if (trkStatCd.equals("관리 중")) {
            trkStatCd = "10";
        } else {
            trkStatCd = "";
        }
        return searchResultRepository.getTraceList(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, trkStatCd, keyword, tsiSearchType, pageRequest);
    }

    public DefaultQueryDtoInterface getTraceInfo(Integer tsrUno) {
        return searchResultRepository.getTraceInfo(tsrUno);
    }

    public List<VideoInfoEntity> getVideoInfoList(Integer tsiUno) {
        return videoInfoRepository.findAllByTsiUno(tsiUno);
    }

    public int getResultByTrace(Integer tsiSearchType){
        return searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(null, tsiSearchType);
    }

    public Map<String, Object> getTraceHistoryList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryList(keyword, tsiSearchType, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryUserFileList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, 10);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceUserFileList(keyword, tsiSearchType, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }
    public Map<String, Object> getTraceHistoryMonitoringList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringList(keyword, tsiSearchType, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoList(keyword, tsiSearchType, pageRequest, tsiUno);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoUserFileList(keyword, tsiSearchType, pageRequest, tsiUno);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }
    
    // 대상자는 없고 대상자키워드 있을때
    public Map<String, Object> getTraceHistoryMonitoringUserFileList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringUserFileList(keyword, tsiSearchType, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqList(keyword, tsiSearchType, pageRequest);

        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqUserFileList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqUserFileList(keyword, tsiSearchType, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoUserFileList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptList(keyword, tsiSearchType, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptUserFileList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptUserFileList(keyword, tsiSearchType, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoUserFileList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoUserFileList(keyword, tsiSearchType, pageRequest, tsiUno);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링
        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }


    public Map<String, Object> allTimeMonitoringList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringList(keyword, tsiSearchType, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링
        return outMap;
    }

    public Map<String, Object> allTimeMonitoringUserFileList(Integer page, String keyword, Integer tsiSearchType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringUserFileList(keyword, tsiSearchType, pageRequest);
        CommonStaticSearchUtil.setOutMap(outMap, traceHistoryListPage);

//        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
//        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));         // 삭제 요청
//        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));       // 삭제 완료
//        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                         // 24시간 모니터링

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_MONITORING, tsiSearchType));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_REQ, tsiSearchType));         // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCdAndTsiSearchType(Consts.TRK_STAT_CD_DEL_CMPL, tsiSearchType));       // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt(tsiSearchType));                                         // 24시간 모니터링

        return outMap;
    }

    public Map<Integer, String> getTsiTypeMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiTypeMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
            tsiTypeMap.put(list.getTsiUno(), list.getTsiType());
        }

        return tsiTypeMap;
    }

    public Map<Integer, String> getTsiKeywordMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, String> tsiKeywordMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
            tsiKeywordMap.put(list.getTsiUno(), list.getTsiKeyword());
        }

        return tsiKeywordMap;
    }

    public Map<Integer, Timestamp> getTsiFstDmlDtMap() {
        List<SearchInfoEntity> searchInfoEntityList = searchInfoRepository.findAllByOrderByTsiUnoDesc();
        Map<Integer, Timestamp> tsiFstDmlDtMap = new HashMap<>();

        for (SearchInfoEntity list : searchInfoEntityList) {
            tsiFstDmlDtMap.put(list.getTsiUno(), list.getFstDmlDt());
        }

        return tsiFstDmlDtMap;
    }

    public String getSearchInfoImgUrl(Integer tsiUno) {
        return searchInfoRepository.getSearchInfoImgUrl(tsiUno);
    }

    public String getSearchInfoTsiType(Integer tsiUno) {
        return searchInfoRepository.getSearchInfoTsiType(tsiUno);
    }

    public List<SearchInfoEntity> getSearchInfoVideoNotReady(){
        List<String> videoList = List.of("15", "19");
        return searchInfoRepository.findTop10ByTsiTypeInAndTsiStatOrderByTsiUnoDesc(videoList, "11");
    }

    public List<SearchInfoEntity> getSearchInfoVideoReady(){
        List<String> videoList = List.of("15", "19");
        return searchInfoRepository.findTop10ByTsiTypeInAndTsiStatOrderByTsiUnoDesc(videoList, "12");
    }

    public void addTrkStat(int userUno,String userId, Integer tsrUno) {
        String trkStatCd = searchResultRepository.getTrkStatCd(tsrUno);
        if ("10".equals(trkStatCd)) {
            searchResultRepository.subTrkStat(tsrUno);
        } else {
            MonitoringHistEntity mtr = new MonitoringHistEntity();
            mtr.setTmhTsrUno(tsrUno);
            mtr.setUserId(userId);
            mtr.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            mtr.setUserUno(userUno);
            monitoringHistRepository.save(mtr);
            searchResultRepository.addTrkStat(tsrUno);
        }
    }

    public void deleteTsiUnos(List<Integer> tsiUnosValue) {
        List<SearchInfoEntity> searchInfoEntity = searchInfoRepository.findByTsiUnoIn(tsiUnosValue);
        for (SearchInfoEntity infoEntity : searchInfoEntity) {
            infoEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
            searchInfoRepository.save(infoEntity);
        }
    }

    public void deleteTsrUnos(List<Integer> tsrUnoValues) {
        List<SearchResultEntity> sre = searchResultRepository.findByTsrUnoIn(tsrUnoValues);
        for (SearchResultEntity searchResultEntity : sre) {
            searchResultEntity.setTrkStatCd(null);
            searchResultEntity.setDataStatCd("20");
            searchResultRepository.save(searchResultEntity);
        }
    }

    public void deleteSearchInfo(Integer tsiUno) {
        SearchInfoEntity searchInfoEntity = searchInfoRepository.findByTsiUno(tsiUno);
        searchInfoEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
        searchInfoRepository.save(searchInfoEntity);
    }

    public void deleteMornitoringInfo(Integer tsrUno) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setDataStatCd(Consts.DATA_STAT_CD_DELETE);
        searchResultRepository.save(searchResultEntity);
        searchResultRepository.stat_co_del(tsrUno);
    }

    public void setTrkHistMemo(Integer tsrUno, String memo) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
        searchResultEntity.setTrkHistMemo(memo);
        searchResultRepository.save(searchResultEntity);
    }

    public void setTrkStatCd(int userUno,String userId, Integer tsrUno, String trkStatCd) {
        if(trkStatCd.equals("20")) { // 삭제요청
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);

            DeleteReqHistEntity deleteReqHistEntity = new DeleteReqHistEntity();
            deleteReqHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            deleteReqHistEntity.setUserId(userId);
            deleteReqHistEntity.setTdrTsrUno(tsrUno);
            deleteReqHistEntity.setUserUno(userUno);

            searchResultRepository.save(searchResultEntity);
            deleteReqHistRepository.save(deleteReqHistEntity);
        } else if(trkStatCd.equals("30")) { // 삭제완료
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);

            DeleteComptHistEntity deleteComptHistEntity = new DeleteComptHistEntity();
            deleteComptHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            deleteComptHistEntity.setTdcTsrUno(tsrUno);
            deleteComptHistEntity.setUserUno(userUno);
            deleteComptHistEntity.setUserId(userId);

            searchResultRepository.save(searchResultEntity);
            deleteComptHistRepository.save(deleteComptHistEntity);
        } else {
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);
            searchResultEntity.setTrkStatCd(trkStatCd);
            searchResultRepository.save(searchResultEntity);
        }
    }
    @Transactional
    public boolean setMonitoringCd(int userUno,String userId, Integer tsrUno, Integer tsrCycleBatch) { // monitoring_cd (10: 비활성화, 20: 활성화)
        try {
            SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);

            searchResultEntity.setMonitoringCd(Consts.MONITORING_CD_NONE.equals(searchResultEntity.getMonitoringCd()) ? Consts.MONITORING_CD_ING : Consts.MONITORING_CD_NONE);
            //TODO 모니터링 주기 설정 html 및 min max 시간 설정 로직
            if (searchResultEntity.getMonitoringCd().equals(Consts.MONITORING_CD_ING)) {
                searchResultEntity.setTsrIsBatch(1);
                searchResultEntity.setTsrCycleBatch(tsrCycleBatch);
            } else {
                searchResultEntity.setTsrIsBatch(0);
            }

            searchResultRepository.save(searchResultEntity);


            // 24시간 모니터링을 On / Off 할 때마다, tb_all_time_monitoring_history table 에 insert
            AlltimeMonitoringHistEntity alltimeMonitoringHistEntity = new AlltimeMonitoringHistEntity();
            alltimeMonitoringHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            alltimeMonitoringHistEntity.setTsrUno(tsrUno);
            alltimeMonitoringHistEntity.setUserUno(userUno);
            alltimeMonitoringHistEntity.setUserId(userId);

            if (searchResultEntity.getMonitoringCd().equals("10")) { // 활성화 될 때
                alltimeMonitoringHistEntity.setTamYn("Y");
            } else {
                alltimeMonitoringHistEntity.setTamYn("N");
            }
            alltimeMonitoringHistRepository.save(alltimeMonitoringHistEntity);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer tsiSearchType, String manageType, String searchUserFile, Integer tsiIsDeployType) {
        log.info("getSearchInfoList page: " + page);
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getSearchInfoResultCnt("10","0", keyword, tsiSearchType, manageType, searchUserFile, tsiIsDeployType, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    // admin 아닐 때
    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer userUno, Integer tsiSearchType, String manageType, String searchUserFile, Integer tsiIsDeployType) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getUserSearchInfoList("10","0", keyword, userUno, tsiSearchType, manageType,searchUserFile, tsiIsDeployType, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    public Map<Integer, String> getUserIdMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByUserUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getUserUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getUserIdByTsiUnoMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByTsiUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getTsiUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getUserIdByTsiUnoSearchTypeMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByTsiUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getTsiUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getProgressPercentMap(List<Integer> tsiUnos) {
        List<SearchJobRepository.ProgressPercentDtoInterface> progressPercentList = searchJobRepository.progressPercentByAll(tsiUnos);
        Map<Integer, String> progressPercentMap = new HashMap<>();

        for (SearchJobRepository.ProgressPercentDtoInterface item : progressPercentList) {
            progressPercentMap.put(item.getTsiUno(), String.format("%d%%", item.getProgressPercent()));
        }

        return progressPercentMap;
    }

    /**
     * 검색 결과 사이트 URL 목록 조회
     *
     * @param tsiUno (검색 정보 테이블의 key)
     * @return List<String> (검색 결과 사이트 URL List)
     */
    public List<String> findTsrSiteUrlDistinctByTsiUno(Integer tsiUno) {
        return searchResultRepository.findTsrSiteUrlDistinctByTsiUno(tsiUno);
    }

    public void searchInfoHistInsert(int userUno, String userId, String searchKeyword, String traceKeyword) {
        SearchInfoHistEntity she = new SearchInfoHistEntity();
        she.setUserUno(userUno);
        she.setUserId(userId);
        she.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        she.setHisKeyword(searchKeyword);

        if(searchKeyword.equals("")) {
            she.setHisKeyword(traceKeyword);
        }
        searchInfoHistRepository.save(she);
    }

    public void traceHistInsert(int userUno,String userId, String keyword) {
        TraceHistEntity the = new TraceHistEntity();
        the.setUserUno(userUno);
        the.setHistKeyword(keyword);
        the.setUserId(userId);
        the.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        traceHistRepository.save(the);
    }

    public void searchResultHistInsert(int userUno, String userId, int histTsiUno) {
        SearchResultHistEntity srh = new SearchResultHistEntity();
        srh.setUserUno(userUno);
        srh.setUserId(userId);
        srh.setHisTsiUno(histTsiUno);
        srh.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        searchResultHistRepository.save(srh);
    }

    public void noticeHistInsert(int userUno, String userId) {
        NoticeHistEntity nhe = new NoticeHistEntity();
        nhe.setUserUno(userUno);
        nhe.setUserId(userId);
        nhe.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        noticeHistRepository.save(nhe);
    }

    public void searchHistoryExcel(HttpServletResponse response
            , List<SearchHistoryExcelDto> searchHistoryExcelDtoList) throws IOException{
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("검색 이력");
        Row row;
        Cell cell;
        int rowNum = 0;

        row=sheet.createRow(rowNum++);

        String[] columnHeader = {"순번", "유포여부", "사례번호", "상담사", "검색유형", "내용", "검색일시", "검색결과", "유사도", "아청물"};
        for(int i=0; i<columnHeader.length; i++){
            cell=row.createCell(i);
            cell.setCellValue(columnHeader[i]);
        }

        for(int i=0; i<searchHistoryExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiUno());

            cell = row.createCell(1);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiIsDeploy());

            cell = row.createCell(2);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getUserFile());

            cell = row.createCell(3);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getUserId());

            cell = row.createCell(4);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiType());

            cell = row.createCell(5);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getKeyword());

            cell = row.createCell(6);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getFstDmlDt());

            cell = row.createCell(7);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getResultCnt());

            cell = row.createCell(8);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTmrSimilarityCnt());

            cell = row.createCell(9);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTmrChildCnt());
        }

        String fileName = "검색 이력";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();

    }

    public void resultExcelList(HttpServletResponse response, List<ResultListExcelDto> resultListExcelDtoList) throws IOException {
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("검색 결과");
        Row row;
        Cell cell;
        int rowNum = 0;

        row=sheet.createRow(rowNum++);

        String[] columnHeader = {"검색이력번호", "결과번호", "SNS", "키워드", "제목", "사이트 URL", "일치율", "사용자 아이디"};
        for(int i=0; i<columnHeader.length; i++){
            cell=row.createCell(i);
            cell.setCellValue(columnHeader[i]);
        }

        for(int i=0; i<resultListExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsiUno());

            cell = row.createCell(1);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrUno());

            cell = row.createCell(2);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrSns());

            cell = row.createCell(3);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsiKeyword());

            cell = row.createCell(4);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrTitle());

            cell = row.createCell(5);
            cell.setCellValue(resultListExcelDtoList.get(i).getTsrSiteUrl());

            cell = row.createCell(6);
            cell.setCellValue(resultListExcelDtoList.get(i).getTmrSimilarity());

            cell = row.createCell(7);
            cell.setCellValue(resultListExcelDtoList.get(i).getUserId());

        }

        String fileName = "검색 결과";
        fileName = URLEncoder.encode(fileName,"UTF-8").replaceAll("\\+", "%20");

        response.setContentType("ms-vnd/excel");
        response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");

        ServletOutputStream out = response.getOutputStream();
        wb.write(out);
        out.flush();
    }

    public void deleteTsrUno(Integer tsrUno) {
        SearchResultEntity sre =  searchResultRepository.findByTsrUno(tsrUno);
        sre.setTrkStatCd(null);
        sre.setDataStatCd("20");
        sre.setMonitoringCd("10");
        searchResultRepository.save(sre);
    }

    public Map<String, Object> getAllUserSearchHistoryList(Integer page, String searchKeyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<UserSearchHistoryDtoInterface> userSearchHistoryList = searchInfoRepository.getAllUserSearchHistoryList(pageRequest, searchKeyword);

        outMap.put("userSearchHistoryList", userSearchHistoryList);
        outMap.put("totalPages", userSearchHistoryList.getTotalPages());
        outMap.put("number", userSearchHistoryList.getNumber());
        outMap.put("totalElements", userSearchHistoryList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    public Map<String, Object> getUserSearchHistoryList(Integer page, String searchKeyword, int userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<UserSearchHistoryDtoInterface> userSearchHistoryList = searchInfoRepository.getUserSearchHistoryList(pageRequest, searchKeyword, userUno);

        outMap.put("userSearchHistoryList", userSearchHistoryList);
        outMap.put("totalPages", userSearchHistoryList.getTotalPages());
        outMap.put("number", userSearchHistoryList.getNumber());
        outMap.put("totalElements", userSearchHistoryList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

    //검색 당시, 서비스, 국가 활성화 확인
    public void saveSearchInfoParams(SearchInfoEntity param){
        try {
            SearchInfoParamsEntity sipEntity = new SearchInfoParamsEntity();
            sipEntity.setTsiUno(param.getTsiUno());
            {

                List<SerpServicesEntity> ssList = serpServicesRepository.findBySsIsActive(1);

                for (SerpServicesEntity item : ssList) {
                    switch (item.getSsName()) {
                        case CommonCode.SerpAPIEngineGoogle -> sipEntity.setTsiIsEngineGoogle(1);
                        case CommonCode.SerpAPIEngineGoogleReverseImage -> sipEntity.setTsiIsEngineGoogleReverseImage(1);
                        case CommonCode.SerpAPIEngineGoogleLens -> sipEntity.setTsiIsEngineGoogleLens(1);
                        case CommonCode.SerpAPIEngineYoutube -> sipEntity.setTsiIsEngineYoutube(1);
                        case CommonCode.SerpAPIEngineBaidu -> sipEntity.setTsiIsEngineBaidu(1);
                        case CommonCode.SerpAPIEngineBing -> sipEntity.setTsiIsEngineBing(1);
                        case CommonCode.SerpAPIEngineDuckduckgo -> sipEntity.setTsiIsEngineDuckduckgo(1);
                        case CommonCode.SerpAPIEngineYahoo -> sipEntity.setTsiIsEngineYahoo(1);
                        case CommonCode.SerpAPIEngineYandex -> sipEntity.setTsiIsEngineYandex(1);
                        case CommonCode.SerpAPIEngineYandexImage -> sipEntity.setTsiIsEngineYandexImage(1);
                        case CommonCode.SerpAPIEngineNaver -> sipEntity.setTsiIsEngineNaver(1);
                    }
                }
            }

            {

                List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);

                for (NationCodeEntity item : ncList) {
                    switch (item.getNcCode()) {
                        case CommonCode.searchNationCodeUs -> sipEntity.setTsiIsNationUs(1);
                        case CommonCode.searchNationCodeKr -> sipEntity.setTsiIsNationKr(1);
                        case CommonCode.searchNationCodeCn -> sipEntity.setTsiIsNationCn(1);
                        case CommonCode.searchNationCodeNl -> sipEntity.setTsiIsNationNl(1);
                        case CommonCode.searchNationCodeTh -> sipEntity.setTsiIsNationTh(1);
                        case CommonCode.searchNationCodeRu -> sipEntity.setTsiIsNationRu(1);
                        case CommonCode.searchNationCodeVn -> sipEntity.setTsiIsNationVn(1);
                    }
                }
            }

            searchInfoParamsRepository.save(sipEntity);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}