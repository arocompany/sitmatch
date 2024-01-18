package com.nex.search.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nex.Chart.entity.*;
import com.nex.Chart.repo.*;
import com.nex.common.CommonCode;
import com.nex.common.CommonStaticSearchUtil;
import com.nex.common.Consts;
import com.nex.search.entity.*;
import com.nex.search.entity.dto.*;
import com.nex.search.entity.result.*;
import com.nex.search.repo.*;
import com.nex.user.entity.ResultListExcelDto;
import com.nex.user.entity.SearchHistoryExcelDto;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.nex.common.CmnUtil.execPython;

@Slf4j
@Service
@Configuration
@RequiredArgsConstructor
public class SearchService {
    private final ImageService imageService;

    private final SearchImageService searchImageService;
    private final SearchImageGoogleLensService searchImageGoogleLensService;
    private final SearchTextService searchTextService;
    private final SearchTextImageService searchTextImageService;

    private final NationCodeRepository nationCodeRepository;

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

    private final ResourceLoader resourceLoader;

    @Value("${file.location2}") private String fileLocation2;
    @Value("${python.video.module}") private String pythonVideoModule;
    @Value("${search.yandex.text.url}") private String textYandexUrl;
    @Value("${search.yandex.text.gl}") private String textYandexGl;
    @Value("${search.yandex.text.no_cache}") private String textYandexNocache;
    @Value("${search.yandex.text.location}") private String textYandexLocation;
    @Value("${search.yandex.text.api_key}") private String textYandexApikey;
    @Value("${search.yandex.image.engine}") private String imageYandexEngine;
    @Value("${search.yandex.text.count.limit}") private String textYandexCountLimit;
    @Value("${file.location1}") private String fileLocation1;
    @Value("${file.location3}") private String fileLocation3;
    @Value("${search.server.url}") private String serverIp2;

    private Boolean loop = true;


    @Bean
    public RestTemplate customRestTemplate() {
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .additionalInterceptors(clientHttpRequestInterceptor())
                .build();
    }

    public ClientHttpRequestInterceptor clientHttpRequestInterceptor() {
        return (request, body, execution) -> {
            RetryTemplate retryTemplate = new RetryTemplate();
            retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
            try {
                return retryTemplate.execute(context -> execution.execute(request, body));
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }

    /*
    @Value("${server.url}")
    private String serverIp;
    @Value("${search.yandex.text.tbm}")
    private String textYandexTbm;
    @Value("${file.location1}")
    private String fileLocation1;
    @Value("${search.yandex.text.engine}")
    private String textYandexEngine;
    */

//    /**
//     * 검색
//     *
//     * @param tsiGoogle    (구글 검색 여부)
//     * @param tsiFacebook  (페이스북 검색 여부)
//     * @param tsiInstagram (인스타그램 검색 여부)
//     * @param tsiTwitter   (트위터 검색 여부)
//     * @param tsiType      (검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상)
//     * @param insertResult (검색 이력 Entity)
//     * @param folder       (저장 폴더)
//     */

    // Facebook, Instagram 도 Google 로 검색, 링크로 Facebook, Instagram 판별
    // byte tsiTwitter,
//    public void search(byte tsiGoogle, byte tsiFacebook, byte tsiInstagram, String tsiType, SearchInfoEntity insertResult, String folder, SearchInfoDto searchInfoDto) throws Exception {
    public void search(SearchInfoEntity param, String folder, SearchInfoDto searchInfoDto) throws Exception {
        if(param.getTsiType() != CommonCode.searchTypeImage && param.getTsiType() != CommonCode.searchTypeVideo){
            if (param.getTsiType().equals(CommonCode.searchTypeKeyword)) {
                // searchYandexYoutube(tsrSns, insertResult, searchInfoDto);
                searchYandexText(CommonCode.snsTypeGoogle, param, searchInfoDto);
            }
            if (param.getTsiGoogle() == 1) { searchGoogle(param.getTsiType(), param, folder, CommonCode.snsTypeGoogle, searchInfoDto); }
            if (param.getTsiFacebook() == 1) { searchGoogle(param.getTsiType(), param, folder, CommonCode.snsTypeFacebook, searchInfoDto); }
            if (param.getTsiInstagram() == 1) { searchGoogle(param.getTsiType(), param, folder, CommonCode.snsTypeInstagram, searchInfoDto); }
            searchGoogle(param.getTsiType(), param, folder, CommonCode.snsTypeGoogle, searchInfoDto);
        }
    }

    /**
     * 구글 검색
     *
     * @param tsiType      (검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상)
     * @param insertResult (검색 이력 Entity)
     * @param folder       (저장 폴더)
     * @param tsrSns       (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     */

    private void searchGoogle(String tsiType, SearchInfoEntity insertResult, String folder, String tsrSns, SearchInfoDto searchInfoDto) throws Exception {
        switch (tsiType) { // 검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지
            case "11":
                log.info("키워드 검색");
                searchYandexByText(tsrSns, insertResult, searchInfoDto);
                break;
            case "13":
                log.info("키워드/이미지 검색");
                searchYandexByTextImage(tsrSns, insertResult, searchInfoDto);
                break;
            case "15":
                log.info("키워드/영상 검색");
                searchYandexByTextVideo(tsrSns, insertResult, searchInfoDto, fileLocation3, folder);
                break;
            case "17":
                log.info("이미지 검색");
                searchYandexByImage(tsrSns, insertResult);
                break;
            case "19":
                log.info("영상 검색");
                searchYandexByVideo(tsrSns, insertResult, fileLocation3, folder);
                break;
        }
    }

    /**
     * Yandex 텍스트 검색
     *
     * @param tsrSns       (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     * @param insertResult (검색 이력 Entity)
     */

    public void searchYandexByText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
        int index=0;

        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        if ("15".equals(tsrSns)) {
            tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue;
        } else if ("17".equals(tsrSns)) {
            tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue;
        }

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
//                    + "&tbm=" + textYandexTbm
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=cn"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=kr"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=th"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=ru"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=vn"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?q=" + tsiKeywordHiddenValue
                    + "&gl=nl"
                    + "&no_cache=" + textYandexNocache
                    + "&location=" + textYandexLocation
                    + "&start=" + (index * 10)
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&engine=google";

            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while (loop);


    }

    /**
     * Yandex 이미지+키워드 검색
     *
     * @param tsrSns       (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     * @param insertResult (검색 이력 Entity)
     */

    public void searchYandexByTextImage(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
        int index = 0;
        loop = true;

        // String tsiKeyword = insertResult.getTsiKeyword();
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp2 + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);
        // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
        // searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

        //인스타
        if ("15".equals(tsrSns)) {
            tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue;
        }
        //페북
        else if ("17".equals(tsrSns)) {
            tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue;
        }

        do {
            String url = textYandexUrl
                    + "?gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=cn"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=kr"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=th"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=ru"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=vn"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index=0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=nl"
                    + "&no_cache=" + textYandexNocache
                    + "&q=" + tsiKeywordHiddenValue
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);
            CompletableFutureText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

    }


    /**
     * Yandex 이미지 검색
     *
     * @param tsrSns       (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     * @param insertResult (검색 이력 Entity)
     */

    public void searchYandexByImage(String tsrSns, SearchInfoEntity insertResult) {
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp2 + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);
        // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
        // searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

        int index = 0;
        loop = true;
        do {
            String url = textYandexUrl
                    + "?gl=" + textYandexGl
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=cn"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=kr"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=th"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=ru"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=vn"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

        index = 0;
        loop=true;

        do {
            String url = textYandexUrl
                    + "?gl=nl"
                    + "&no_cache=" + textYandexNocache
                    + "&api_key=" + textYandexApikey
                    + "&safe=off"
                    + "&filter=0"
                    + "&nfpr=0"
                    + "&start=" + (index * 10)
                    // + "&tbm=" + textYandexTbm
                    + "&engine=" + imageYandexEngine
                    + "&image_url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                loop = false;
            }

            index++;
        } while (loop);

    }


//    /**
//     * @deprecated 2023-03-26 사용 중지, 메소드 2개로 분리 {@link #searchYandex(String, Class, Function, Function)}, {@link #saveYandex(List, String, SearchInfoEntity, Function, Function, Function, Function, Function, Function)}
//     */
//    @Deprecated
//    public List<SearchResultEntity> searchYandexByText(String url, String tsrSns, SearchInfoEntity insertResult) throws Exception {
//        String jsonInString = "";
//        // RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders header = new HttpHeaders();
//        HttpEntity<?> entity = new HttpEntity<>(header);
//        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
//        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
//        List<SearchResultEntity> sreList = new ArrayList<SearchResultEntity>();
//        if (resultMap.getStatusCodeValue() == 200) {
//
//            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            jsonInString = mapper.writeValueAsString(resultMap.getBody());
//            YandexByTextResult yandexByTextResult = mapper.readValue(jsonInString, YandexByTextResult.class);
//            if (yandexByTextResult.getError() == null) {
//
//                SearchResultEntity sre = null;
//                for (Images_resultsByText images_result : yandexByTextResult.getImages_results()) {
//                    try {
//                        sre = new SearchResultEntity();
//                        sre.setTsiUno(insertResult.getTsiUno());
//                        sre.setTsrJson(images_result.toString());
//                        sre.setTsrDownloadUrl(images_result.getOriginal());
//                        sre.setTsrTitle(images_result.getTitle());
//                        sre.setTsrSiteUrl(images_result.getLink());
//                        //sre.setTsrSns("11");
//
//                        //2023-03-20
//                        //Facebook, Instagram 도 Google 로 검색, source 값으로 Facebook, Instagram 판별
//
//                        //Facebook 검색이고, source 값이 Facebook 인 경우
//                        if ("17".equals(tsrSns) && images_result.isFacebook()) {
//                            sre.setTsrSns("17");
//                        }
//                        //Instagram 검색이고, source 값이 Instagram 인 경우
//                        else if ("15".equals(tsrSns) && images_result.isInstagram()) {
//                            sre.setTsrSns("15");
//                        }
//                        //그 외는 구글
//                        else {
//                            sre.setTsrSns("11");
//                        }
//
//
//                        //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
//                        if (!tsrSns.equals(sre.getTsrSns())) {
//                            continue;
//                        }
//
//
//                        //Resource resource = resourceLoader.getResource(imageUrl);
//                        //2023-03-21
//                        //구글은 original, Facebook, Instagram 는 thumbnail 로 값을 가져오도록 변경
//                        String imageUrl = "11".equals(sre.getTsrSns()) ? images_result.getOriginal() : images_result.getThumbnail();
//                        Resource resource = resourceLoader.getResource(imageUrl);
//
//
//                        if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("")) {
//
//                            LocalDate now = LocalDate.now();
//                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//                            String folder = now.format(formatter);
//                            String restrictChars = "|\\\\?*<\":>/";
//                            String regExpr = "[" + restrictChars + "]+";
//                            String uuid = UUID.randomUUID().toString();
//                            String extension = "";
//                            String extension_ = "";
//                            if (resource.getFilename().indexOf(".") > 0) {
//                                extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
//                                extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
//                                extension_ = extension.substring(1);
//                            }
//
//
//                            byte[] imageBytes = customRestTemplate().getForObject(imageUrl, byte[].class);
//                            File destdir = new File(fileLocation2 + folder + File.separator + insertResult.getTsiUno());
//                            if (!destdir.exists()) {
//                                destdir.mkdirs();
//                            }
//
//                            Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
//                            sre.setTsrImgExt(extension_);
//                            sre.setTsrImgName(uuid + extension);
//                            sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));
//
//                            Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
//                            sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
//                            sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
//                            sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
//                            img.flush();
//                        }
//                        log.debug("url ::::: " + url + "    +++++++++++++++++++++++      sre ::::: " + sre);
//
//                        saveSearchResult(sre);
//                        sreList.add(sre);
//                    } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
//                        log.error(e.getMessage());
//                        e.printStackTrace();
//                        throw new IOException(e);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        log.error(e.getMessage());
//                    }
//                }
//            }
//        }
//
//        return sreList;
//    }

    /**
     * RESULT 로 검색 결과 엔티티 추출
     *
     * @param tsiUno        (검색 정보 테이블의 key)
     * @param tsrSns        (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     * @param result        (결과)
     * @param getOriginalFn (original getter Function)
     * @param getTitleFn    (title getter Function)
     * @param getLinkFn     (link getter Function)
     * @param isFacebookFn  (isFacebook Function)
     * @param isInstagramFn (isInstagram Function)
     * @param <RESULT>      (결과)
     * @return RESULT        (결과)
     */



    public <RESULT> SearchResultEntity getSearchResultEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) {
        log.info("searchResultEntity: "+getTitleFn+" getLinkFn: " + getLinkFn);
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));

        log.info("setTsrSiteUrl: " + getLinkFn.apply(result));
        //sre.setTsrSns("11");

        //Facebook 검색이고, source 값이 Facebook 인 경우
        if ("17".equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns("17");
        } else if ("15".equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns("15");
        } else {
            sre.setTsrSns("11");
        }

        return sre;
    }



    public <RESULT> SearchResultEntity getYoutubeResultEntity(int tsiUno, RESULT result
            , Function<RESULT, String> getPositionFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getTitleFn) {
        log.info("searchResultEntity: "+getTitleFn+" getLinkFn: " + getLinkFn);
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getPositionFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));

        log.info("setTsrSiteUrl: " + getLinkFn.apply(result));
        sre.setTsrSns("11");

        return sre;
    }

//    /**
//     * 이미지 파일 저장
//     *
//     * @param tsiUno         (검색 정보 테이블의 key)
//     * @param restTemplate   (RestTemplate)
//     * @param sre            (검색 결과 엔티티)
//     * @param result         (결과)
//     * @param getOriginalFn  (original getter Function)
//     * @param getThumbnailFn (thumbnail getter Function)
//     * @param <RESULT>       (결과)
//     */

    /*
    public <RESULT> void saveImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn) throws IOException {
        //Resource resource = resourceLoader.getResource(imageUrl);
        //구글은 original, Facebook, Instagram 는 thumbnail 로 값을 가져오도록 변경

        String imageUrl = "11".equals(sre.getTsrSns()) ? getOriginalFn.apply(result) : getThumbnailFn.apply(result);
        imageUrl = imageUrl != null ? getOriginalFn.apply(result) : getThumbnailFn.apply(result);

        //2023-03-26 에러 나는 url 처리
        byte[] imageBytes = null;
        if (imageUrl != null) {
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                //구글인 경우 IGNORE
//                if ("11".equals(sre.getTsrSns())) {
                imageUrl = getThumbnailFn.apply(result);
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//                }
//                else {
//                    log.error(e.getMessage(), e);
//                    System.out.println("catch else e"+e.getMessage());
//                    throw new RuntimeException(e);
//                }
            }

            //2023-03-26 에러가 안나도 imageBytes 가 null 일 때가 있음
            if (imageBytes == null) {
                imageUrl = getThumbnailFn.apply(result);
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            }

            if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("") && imageBytes != null) {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String folder = now.format(formatter);
                String restrictChars = "|\\\\?*<\":>/";
                String regExpr = "[" + restrictChars + "]+";
                String uuid = UUID.randomUUID().toString();
                String extension = "";
                String extension_ = "";
                if (resource.getFilename().indexOf(".") > 0) {
                    extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                    extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                    extension_ = extension.substring(1);
                }

                File destdir = new File(fileLocation2 + folder + File.separator + tsiUno);
                if (!destdir.exists()) {
                    destdir.mkdirs();
                }

                Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                sre.setTsrImgExt(extension_);
                sre.setTsrImgName(uuid + extension);
                sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                img.flush();
            }
        } else {

        }

    }
    */





    public <RESULT> void saveYoutubeImageFile(int tsiUno, RestTemplate restTemplate, SearchResultEntity sre
            , RESULT result, Function<RESULT, Map<String,String>> getThumnailFn) throws IOException {
        // Function<RESULT, String> getPositionFn,
        log.info("saveYoutubeImageFile 진입 ===============");
        log.info("getThumbnailFn: " + getThumnailFn);
        // Map<String, String> imageUrl = "11".equals(sre.getTsrSns()) ? getPositionFn.apply(result) : getThumnailFn.apply(result);
        String imageUrl = getThumnailFn.apply(result).get("static");
        // imageUrl = imageUrl.replace("%7Bstatic%3Dhttps","https");

        log.info("imageUrl: "+imageUrl);
        // imageUrl = imageUrl != null ? getPositionFn.apply(result) : getThumnailFn.apply(result);

        //2023-03-26 에러 나는 url 처리
        byte[] imageBytes = null;
        if (imageUrl != null) { // .toString()
            Resource resource = resourceLoader.getResource(imageUrl);
            try {
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            } catch (Exception e) {
                //구글인 경우 IGNORE
//                if ("11".equals(sre.getTsrSns())) {
                imageUrl = getThumnailFn.apply(result).toString();
                imageUrl = imageUrl.replace("%7Bstatic%3Dhttps","https");
                resource = resourceLoader.getResource(imageUrl);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//                }
//                else {
//                    log.error(e.getMessage(), e);
//                    System.out.println("catch else e"+e.getMessage());
//                    throw new RuntimeException(e);
//                }
            }

            // 에러가 안나도 imageBytes 가 null 일 때가 있음
            if (imageBytes == null) {
                imageUrl = getThumnailFn.apply(result).toString();
                log.debug("imageUrl: "+imageUrl);

                resource = resourceLoader.getResource(imageUrl);
                log.debug("resource: " + resource);
                imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                log.debug("imageBytes: "+  restTemplate.getForObject(imageUrl, byte[].class) );
            }

            if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("") && imageBytes != null) {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String folder = now.format(formatter);
                String restrictChars = "|\\\\?*<\":>/";
                String regExpr = "[" + restrictChars + "]+";
                String uuid = UUID.randomUUID().toString();
                String extension = "";
                String extension_ = "";
                if (resource.getFilename().indexOf(".") > 0) {
                    extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                    extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                    extension_ = extension.substring(1);
                }

                File destdir = new File(fileLocation2 + folder + File.separator + tsiUno);
                if (!destdir.exists()) {
                    destdir.mkdirs();
                }

                Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                sre.setTsrImgExt(extension_);
                sre.setTsrImgName(uuid + extension);
                sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                img.flush();
            }
        } else {

        }

    }

    //    public SearchResultEntity saveSearchResult(SearchResultEntity sre) {
//        setSearchResultDefault(sre);
//        return searchResultRepository.save(sre);
//    }

//    /**
//     * 검색 작업 저장
//     *
//     * @param result       (검색 결과 엔티티 List)
//     * @param insertResult (검색 이력 엔티티)
//     * @return String       (저장 결과)
//     */
    public String saveImgSearchYandex(List<SearchResultEntity> result, SearchInfoEntity insertResult) {
        insertResult.setTsiStat("13");

        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
        }
        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
        // SearchInfoEntity updateResult = saveSearchInfo_2(insertResult);
//        saveSearchInfo_2(insertResult);
        CommonStaticSearchUtil.setSearchInfoDefault_2(insertResult);
        searchInfoRepository.save(insertResult);
        List<SearchResultEntity> searchResultEntity = result;

        for (SearchResultEntity sre : searchResultEntity) {
            try {
                SearchJobEntity sje = CommonStaticSearchUtil.getSearchJobEntity(sre);
                CommonStaticSearchUtil.setSearchJobDefault(sje);
                searchJobRepository.save(sje);
            } catch (JpaSystemException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                throw new JpaSystemException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return "저장 완료";
    }

//    /**
//     * @Deprecated 2023-03-26 사용 중지 {@link #saveImgSearchYandex(List, SearchInfoEntity)}
//     */
//    @Deprecated
//    public String saveImgSearchYandexByText(List<SearchResultEntity> result, SearchInfoEntity insertResult) throws Exception {
//
//        insertResult.setTsiStat("13");
//        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
//            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
//        }
//        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
//        saveSearchInfo(insertResult);
//        List<SearchResultEntity> searchResultEntity = result;
//
//        for (SearchResultEntity sre : searchResultEntity) {
//            try {
//                SearchJobEntity sje = getSearchJobEntity(sre);
//                saveSearchJob(sje);
//            } catch (JpaSystemException e) {
//                log.error(e.getMessage());
//                e.printStackTrace();
//                throw new JpaSystemException(e);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//                e.printStackTrace();
//            }
//        }
//
//        return "저장 완료";
//    }
//
    /**
     * @deprecated 2023-03-26 사용 중지, 메소드 2개로 분리 {@link #searchYandex(String, Class, Function, Function)}, {@link #saveYandex(List, String, SearchInfoEntity, Function, Function, Function, Function, Function, Function)}
     */
    @Deprecated
    public List<SearchResultEntity> searchYandexByImage(String url, String tsrSns, SearchInfoEntity insertResult) throws Exception {
        String jsonInString = "";
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<SearchResultEntity> sreList = new ArrayList<SearchResultEntity>();
        if (resultMap.getStatusCodeValue() == 200) {

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonInString = mapper.writeValueAsString(resultMap.getBody());
            YandexByImageResult YandexByImageResult = mapper.readValue(jsonInString, YandexByImageResult.class);
            if (YandexByImageResult.getError() == null) {

                SearchResultEntity sre = null;
                for (Images_resultsByImage images_result : YandexByImageResult.getInline_images()) {
                    try {

                        String imageUrl = images_result.getOriginal();
                        sre = new SearchResultEntity();
                        sre.setTsiUno(insertResult.getTsiUno());
                        sre.setTsrJson(images_result.toString());
                        sre.setTsrDownloadUrl(imageUrl);
                        sre.setTsrTitle(images_result.getTitle());
                        sre.setTsrSiteUrl(images_result.getLink());
                        //sre.setTsrSns("11");

                        //Facebook 검색이고, source 값이 Facebook 인 경우
                        if ("17".equals(tsrSns) && images_result.isFacebook()) {
                            sre.setTsrSns("17");
                        }
                        //Instagram 검색이고, source 값이 Instagram 인 경우
                        else if ("15".equals(tsrSns) && images_result.isInstagram()) {
                            sre.setTsrSns("15");
                        }
                        //그 외는 구글
                        else {
                            sre.setTsrSns("11");
                        }

                        //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                        if (!tsrSns.equals(sre.getTsrSns())) {
                            continue;
                        }

                        //Resource resource = resourceLoader.getResource(imageUrl);
                        Resource resource = resourceLoader.getResource("11".equals(sre.getTsrSns()) ? imageUrl : images_result.getThumbnail());
                        if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("")) {

                            LocalDate now = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                            String folder = now.format(formatter);
                            String restrictChars = "|\\\\?*<\":>/";
                            String regExpr = "[" + restrictChars + "]+";
                            String uuid = UUID.randomUUID().toString();
                            String extension = "";
                            String extension_ = "";
                            if (resource.getFilename().indexOf(".") > 0) {
                                extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                                extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                                extension_ = extension.substring(1);
                            }


                            byte[] imageBytes = customRestTemplate().getForObject(imageUrl, byte[].class);
                            File destdir = new File(fileLocation2 + folder + File.separator + insertResult.getTsiUno());
                            if (!destdir.exists()) {
                                destdir.mkdirs();
                            }

                            Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                            sre.setTsrImgExt(extension_);
                            sre.setTsrImgName(uuid + extension);
                            sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                            Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                            sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                            sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                            sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                            img.flush();

                        }
                        CommonStaticSearchUtil.setSearchResultDefault(sre);
                        searchResultRepository.save(sre);

                        sreList.add(sre);
                    } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                        log.error(e.getMessage());
                        e.printStackTrace();
                        throw new IOException(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                }
            }
        }

        return sreList;
    }

    /*
     * Images_resultsByImage 추출
     *
     * @param url (검색 Url)
     * @return List<Images_resultsByImage> (Images_resultsByImage List)
     * @throws Exception
     */
    /*
    public List<Images_resultsByImage> searchYandexByImage(String url) throws Exception {

        String jsonInString = "";
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<Images_resultsByImage> inlineImages = null;

        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonInString = mapper.writeValueAsString(resultMap.getBody());
            YandexByImageResult YandexByImageResult = mapper.readValue(jsonInString, YandexByImageResult.class);

            if (YandexByImageResult.getError() == null) {
                inlineImages = YandexByImageResult.getInline_images();
            }
        }

        return inlineImages;
    }
    */

//    /**
//     * 텍스트 검색
//     * {@link #searchYandexByText(String, String, SearchInfoEntity)} {@link #searchYandexByImage(String, String, SearchInfoEntity)}}
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

    public <INFO, RESULT> List<RESULT> searchTextYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        // ResponseEntity<?> resultMap = new customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<RESULT> results = null;

        log.info("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());
        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "images_results");
            // String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("organic_results", "organic_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
            log.info("mapper: "+ mapper);
            log.info("jsonInString: "+ jsonInString);
            log.info("info: "+ info);
            log.info("result: "+results);

        }
        return results != null ? results : new ArrayList<>();
    }

//    /**
//     * 텍스트, 이미지 검색
//     *
//     * {@link #searchYandexByText(String, String, SearchInfoEntity)} {@link #searchYandexByImage(String, String, SearchInfoEntity)}}
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

    // 배치시 진입
    public <INFO, RESULT> List<RESULT> searchBatchYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("searchBatchYandex 진입");
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        // ResponseEntity<?> resultMap = new customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        List<RESULT> results = null;

        log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "images_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
        }

        log.debug("results: " + results);
        return results != null ? results : new ArrayList<>();
    }

    public <INFO, RESULT> List<RESULT> searchYandex(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("searchYandex 진입");
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        List<RESULT> results = null;

        log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "images_results");
            INFO info = mapper.readValue(jsonInString, infoClass);

            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
        }

        log.debug("results: " + results);
        return results != null ? results : new ArrayList<>();
    }

    // 유튜브
    public <INFO, RESULT> List<RESULT> searchByYoutube(String url, Class<INFO> infoClass, Function<INFO, String> getErrorFn, Function<INFO, List<RESULT>> getResultFn) throws Exception {
        log.info("searchByYoutube 진입");
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = customRestTemplate().exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

        List<RESULT> results = null;

        log.debug("resultMap.getStatusCodeValue(): " + resultMap.getStatusCodeValue());

        if (resultMap.getStatusCodeValue() == 200) {
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // String jsonInString = mapper.writeValueAsString(resultMap.getBody()).replace("image_results", "video_results");
            String jsonInString = mapper.writeValueAsString(resultMap.getBody());
            log.info("jsonInString "+jsonInString);
            INFO info = mapper.readValue(jsonInString, infoClass);

            log.info("info: "+info);
            if (getErrorFn.apply(info) == null) {
                results = getResultFn.apply(info);
            }
        }

        log.debug("results: " + results);
        return results != null ? results : new ArrayList<>();
    }

    /**
     * 검색 결과 저장
     *
     * @param results        (RESULT List)
     * @param tsrSns         (SNS 아이콘(11 : 구글, 13 : 트위터, 15 : 인스타, 17 : 페북))
     * @param insertResult   (검색 이력 엔티티)
     * @param getOriginalFn  (original getter Function)
     * @param getThumbnailFn (thumbnail getter Function)
     * @param getTitleFn     (title getter Function)
     * @param getLinkFn      (link getter Function)
     * @param isFacebookFn   (isFacebook Function)
     * @param isInstagramFn  (isInstagram Function)
     * @param <RESULT>       (Images_resultsByText or Images_resultsByImage)
     * @return List<SearchResultEntity> (검색 결과 엔티티 List)
     * @throws Exception
     */
    public <RESULT> List<SearchResultEntity> saveYandex(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getThumbnailFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws Exception {
        log.info("========= saveYandex 진입 =========");

        if (results == null) {
            log.info("result null");
            return null;
        }

        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();

        //SearchResultEntity sre = null;
        for (RESULT result : results) {
            log.info("results: " + results);

            try {
                String imageUrl = getOriginalFn.apply(result);
                log.info("imageUrl1: "+imageUrl);
                if(imageUrl == null) {
                    imageUrl = getThumbnailFn.apply(result);
                }
                log.info("imageUrl2: "+imageUrl);
                if(imageUrl != null) {
                    //검색 결과 엔티티 추출
                    SearchResultEntity sre = getSearchResultEntity(insertResult.getTsiUno(), tsrSns, result, getOriginalFn, getTitleFn, getLinkFn, isFacebookFn, isInstagramFn);

                    //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                    if (!tsrSns.equals(sre.getTsrSns())) {
                        continue;
                    }

                    log.info("getThumbnailFn: "+getThumbnailFn);

                    //이미지 파일 저장
                    imageService.saveImageFile(insertResult.getTsiUno(), customRestTemplate(), sre, result, getOriginalFn, getThumbnailFn, false);
                    CommonStaticSearchUtil.setSearchResultDefault(sre);
                    searchResultRepository.save(sre);
                    sreList.add(sre);
                }
            } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }

    // ,Function<RESULT, String> getThumnailFn
    public <RESULT> List<SearchResultEntity> saveYoutube(List<RESULT> results, String tsrSns, SearchInfoEntity insertResult
            , Function<RESULT, String> getPositionFn, Function<RESULT, String> getLinkFn, Function<RESULT, String> getTitleFn ,Function<RESULT, Map<String,String>> getThumnailFn) throws Exception {
        log.info("========= saveYandex 진입 =========");

        log.info("getThumbnailFn: " + getThumnailFn);

        if (results == null) {
            log.info("result null");
            return null;
        }

        // RestTemplate restTemplate = new RestTemplate();
        List<SearchResultEntity> sreList = new ArrayList<>();

        //SearchResultEntity sre = null;
        for (RESULT result : results) {
            log.info("results: " + results);
            try {
                //검색 결과 엔티티 추출
                SearchResultEntity sre = getYoutubeResultEntity(insertResult.getTsiUno(), result, getPositionFn,getLinkFn, getTitleFn);

                //Facebook, Instagram 인 경우 SNS 아이콘이 구글 인 경우 스킵
                if (!tsrSns.equals(sre.getTsrSns())) {
                    continue;
                }

                //이미지 파일 저장
                saveYoutubeImageFile(insertResult.getTsiUno(), customRestTemplate(), sre, result, getThumnailFn);
                CommonStaticSearchUtil.setSearchResultDefault(sre);
                searchResultRepository.save(sre);

                sreList.add(sre);

            } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                log.error(e.getMessage());
                throw new IOException(e);

            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return sreList;
    }

    /*
    public List<SearchResultEntity> searchYandexByImage2(String url, String tsrSns, SearchInfoEntity insertResult) throws Exception {
        String jsonInString = "";
        // RestTemplate restTemplate = new RestTemplate();
        HttpHeaders header = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(header);
        UriComponents uri = UriComponentsBuilder.fromHttpUrl(url).build();
        ResponseEntity<?> resultMap = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);
        List<SearchResultEntity> sreList = new ArrayList<>();
        if (resultMap.getStatusCodeValue() == 200) {

            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            jsonInString = mapper.writeValueAsString(resultMap.getBody());
            YandexByImageResult YandexByImageResult = mapper.readValue(jsonInString, YandexByImageResult.class);
            if (YandexByImageResult.getError() == null) {

                SearchResultEntity sre = null;
                for (Images_resultsByImage images_result : YandexByImageResult.getInline_images()) {
                    try {
                        String imageUrl = images_result.getOriginal();
                        sre = new SearchResultEntity();
                        sre.setTsiUno(insertResult.getTsiUno());
                        sre.setTsrJson(images_result.toString());
                        sre.setTsrDownloadUrl(imageUrl);
                        sre.setTsrTitle(images_result.getTitle());
                        sre.setTsrSiteUrl(images_result.getLink());
                        //sre.setTsrSns("11");

                        //Facebook 검색이고, source 값이 Facebook 인 경우
                        if ("17".equals(tsrSns) && images_result.isFacebook()) {
                            sre.setTsrSns("17");
                        }
                        //Instagram 검색이고, source 값이 Instagram 인 경우
                        else if ("15".equals(tsrSns) && images_result.isInstagram()) {
                            sre.setTsrSns("15");
                        }
                        //그 외는 구글
                        else {
                            sre.setTsrSns("11");
                        }

                        if (!tsrSns.equals(sre.getTsrSns())) {
                            continue;
                        }

                        //Resource resource = resourceLoader.getResource(imageUrl);
                        Resource resource = resourceLoader.getResource("11".equals(sre.getTsrSns()) ? imageUrl : images_result.getThumbnail());

                        if (resource.getFilename() != null && !resource.getFilename().equalsIgnoreCase("")) {

                            LocalDate now = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                            String folder = now.format(formatter);
                            String restrictChars = "|\\\\?*<\":>/";
                            String regExpr = "[" + restrictChars + "]+";
                            String uuid = UUID.randomUUID().toString();
                            String extension = "";
                            String extension_ = "";
                            if (resource.getFilename().indexOf(".") > 0) {
                                extension = resource.getFilename().substring(resource.getFilename().lastIndexOf("."));
                                extension = extension.replaceAll(regExpr, "").substring(0, Math.min(extension.length(), 10));
                                extension_ = extension.substring(1);
                            }

                            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
                            File destdir = new File(fileLocation2 + folder + File.separator + insertResult.getTsiUno());
                            if (!destdir.exists()) {
                                destdir.mkdirs();
                            }

                            Files.write(Paths.get(destdir + File.separator + uuid + extension), imageBytes);
                            sre.setTsrImgExt(extension_);
                            sre.setTsrImgName(uuid + extension);
                            sre.setTsrImgPath((destdir + File.separator).replaceAll("\\\\", "/"));

                            Image img = new ImageIcon(destdir + File.separator + uuid + extension).getImage();
                            sre.setTsrImgHeight(String.valueOf(img.getHeight(null)));
                            sre.setTsrImgWidth(String.valueOf(img.getWidth(null)));
                            sre.setTsrImgSize(String.valueOf(destdir.length() / 1024));
                            img.flush();

                        }
                        saveSearchResult(sre);
                        sreList.add(sre);
                    } catch (IOException e) {// IOException 의 경우 해당 Thread 를 종료하도록 처리.
                        log.error(e.getMessage());
                        e.printStackTrace();
                        throw new IOException(e);
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return sreList;
    }
    */

//    /**
//     * @Deprecated 2023-03-26 사용 중지 {@link #saveImgSearchYandex(List, SearchInfoEntity)}
//     */
//    @Deprecated
//    public String saveImgSearchYandexByImage(List<SearchResultEntity> result, SearchInfoEntity insertResult) throws Exception {
//        CompletableFuture.allOf().join();
//
//        insertResult.setTsiStat("13");
//        if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
//            insertResult.setTsiImgPath(insertResult.getTsiImgPath().replaceAll("\\\\", "/"));
//        }
//
//        // SearchInfoEntity updateResult = saveSearchInfo(insertResult);
//        saveSearchInfo(insertResult);
//
//        // List<SearchResultEntity> searchResultEntity = result;
//        SearchJobEntity sje = null;
//        // for (SearchResultEntity sre : searchResultEntity) {
//        for (SearchResultEntity sre : result) {
//            try {
//                sje = new SearchJobEntity();
//                sje.setTsiUno(sre.getTsiUno());
//                sje.setTsrUno(sre.getTsrUno());
//                if (insertResult.getTsiImgPath() != null && !insertResult.getTsiImgPath().isEmpty()) {
//                    sje.setTsrImgPath(sre.getTsrImgPath().replaceAll("\\\\", "/"));
//                } else {
//                    sje.setTsrImgPath("");
//                }
//                sje.setTsrImgName(sre.getTsrImgName());
//                sje.setTsrImgExt(sre.getTsrImgExt());
//                saveSearchJob(sje);
//            } catch (JpaSystemException e) {
//                log.error(e.getMessage());
//                e.printStackTrace();
//                throw new JpaSystemException(e);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//                e.printStackTrace();
//            }
//        }
//
//        return "저장 완료";
//    }

    public List<String> processVideo(SearchInfoEntity insertResult) throws Exception {
        // List<String> files = new ArrayList<String>();
        List<String> files = new ArrayList<>();

        log.debug("Python Call");
        String[] command = new String[4];
        //python C:/utils/extract_keyframes.py C:/utils/input_Vid.mp4 C:/data/requests/20230312
        command[0] = "python";
        command[1] = pythonVideoModule;
        command[2] = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        command[3] = insertResult.getTsiImgPath() + insertResult.getTsiUno();
        try {
            execPython(command);

            String DATA_DIRECTORY = insertResult.getTsiImgPath() + insertResult.getTsiUno() + "/";
            File dir = new File(DATA_DIRECTORY);

            String[] filenames = dir.list();
            for (String filename : filenames) {
                files.add(insertResult.getTsiImgPath() + insertResult.getTsiUno() + "/" + filename);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }

        return files;
    }

    // Yandex 영상 검색 후처리 1107 원본
    /*@Async
    public void searchYandexByVideo(String tsrSns, SearchInfoEntity insertResult, String folder, String location3) {
        try {
            List<String> files = processVideo(insertResult);
            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=" + textYandexGl
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            List<String> files = processVideo(insertResult);
            log.info("cn 검색 진입");

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=cn"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            List<String> files = processVideo(insertResult);
            log.info("kr 검색 진입");

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=kr"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        try {
            List<String> files = processVideo(insertResult);

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=th"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        try {
            List<String> files = processVideo(insertResult);

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=ru"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


        try {
            List<String> files = processVideo(insertResult);

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=vn"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            List<String> files = processVideo(insertResult);

            for (int i = 0; i < files.size(); i++) {
                VideoInfoEntity videoInfo = new VideoInfoEntity();
                videoInfo.setTsiUno(insertResult.getTsiUno());
                videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
                videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
                saveVideoInfo(videoInfo);

                String searchImageUrl = serverIp + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                String url = textYandexUrl
                        + "?gl=nl"
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
                                return searchYandexByImage(url, tsrSns, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        })
                        .thenApplyAsync((r) -> {
                            try {
                                // yandex검색을 통해 결과 db에 적재.
                                return saveImgSearchYandex(r, insertResult);
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                return null;
                            }
                        });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    */

    @Async
    public void searchYandexByVideo(String tsrSns, SearchInfoEntity insertResult, String folder, String location3) throws Exception {
        List<String> files = processVideo(insertResult);

        for(int i=0; i<files.size(); i++) {
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setTsiUno(insertResult.getTsiUno());
            videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
            videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
            saveVideoInfo(videoInfo);
        }

        try {
            for (int i = 0; i < files.size(); i++) {
                String searchImageUrl = serverIp2 + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                // searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                try {
                    String url = textYandexUrl
                            + "?gl=cn"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=" + textYandexGl
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=kr"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });

                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=th"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });

                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=ru"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=vn"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=nl"
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }


            }

        } catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }

    // 키워드+영상
    @Async
    public void searchYandexByTextVideo(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto, String folder, String location3) throws Exception {
        List<String> files = processVideo(insertResult);

        for(int i=0; i<files.size(); i++) {
            VideoInfoEntity videoInfo = new VideoInfoEntity();
            videoInfo.setTsiUno(insertResult.getTsiUno());
            videoInfo.setTviImgName(files.get(i).substring(files.get(i).lastIndexOf("/") + 1));
            videoInfo.setTviImgRealPath(files.get(i).substring(0, files.get(i).lastIndexOf("/") + 1));
            saveVideoInfo(videoInfo);
        }

        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        if ("15".equals(tsrSns)) {
            tsiKeywordHiddenValue = "인스타그램 " + tsiKeywordHiddenValue;
        } else if ("17".equals(tsrSns)) {
            tsiKeywordHiddenValue = "페이스북 " + tsiKeywordHiddenValue;
        }

        try {
            for (int i = 0; i < files.size(); i++) {
                String searchImageUrl = serverIp2 + folder + "/" + location3 + "/" + insertResult.getTsiUno() + files.get(i).substring(files.get(i).lastIndexOf("/"));
                // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
                // searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");

                try {
                    String url = textYandexUrl
                            + "?gl=" + textYandexGl
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });

                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=cn"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=kr"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=th"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=ru"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=vn"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                try {
                    String url = textYandexUrl
                            + "?gl=nl"
                            + "&q=" + tsiKeywordHiddenValue
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
                                    return searchYandexByImage(url, tsrSns, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            })
                            .thenApplyAsync((r) -> {
                                try {
                                    // yandex검색을 통해 결과 db에 적재.
                                    return saveImgSearchYandex(r, insertResult);
                                } catch (Exception e) {
                                    log.error(e.getMessage(), e);
                                    return null;
                                }
                            });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

//    public SearchJobEntity saveSearchJob(SearchJobEntity sje) {
//        CommonStaticSearchUtil.setSearchJobDefault(sje);
//        return searchJobRepository.save(sje);
//    }

    public SearchInfoEntity saveSearchInfo(SearchInfoEntity sie) {
        CommonStaticSearchUtil.setSearchInfoDefault(sie);
        return searchInfoRepository.save(sie);
    }

//    public SearchInfoEntity saveSearchInfo_2(SearchInfoEntity sie) {
//        CommonStaticSearchUtil.setSearchInfoDefault_2(sie);
//        return searchInfoRepository.save(sie);
//    }


    public VideoInfoEntity saveVideoInfo(VideoInfoEntity vie) {
        vie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        vie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        return videoInfoRepository.save(vie);
    }

//    public SearchResultEntity saveSearchResult(SearchResultEntity sre) {
//        setSearchResultDefault(sre);
//        return searchResultRepository.save(sre);
//    }

//    /**
//     * 검색 결과 엔티티 기본값 세팅
//     *
//     * @param sre (검색 결과 엔티티)
//     */


    public Page<DefaultQueryDtoInterface> getSearchResultList(Integer tsiUno, String keyword, Integer page, String priority,
                                                              String tsjStatus1, String tsjStatus2, String tsjStatus3, String tsjStatus4,
                                                              String snsStatus01, String snsStatus02, String snsStatus03, String snsStatus04, String order_type) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        log.debug("priority => {}", priority);

        // String orderByTmrSimilarityDesc = " ORDER BY tmrSimilarity desc, TMR.TSR_UNO desc";

        if ("1".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_1");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_1(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("2".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_2");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_2(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("3".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_3");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_3(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("4".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_4");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_4(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("5".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_5");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_5(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else if ("6".equals(order_type)) {
            log.info("getResultInfoListOrderByTmrSimilarityDesc_6");
            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc_6(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        } else {
            log.info("getResultInfoListOrderByTmrSimilarityDesc");
            log.info("pageRequest" + pageRequest);

            return searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                    snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
        }
    }

    // Integer percent,
    public Page<DefaultQueryDtoInterface> getNoticeList(Integer page, Integer tsiUno, String tsiKeyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        log.info("pageRequest: " + pageRequest);
        log.info("tsiuno: " + tsiUno);

        if (tsiUno == 0) {
            // return searchResultRepository.getNoticeList(pageRequest, percent);
            return searchResultRepository.getNoticeList(pageRequest);
        } else {
            return StringUtils.hasText(tsiKeyword) ? searchResultRepository.getNoticeSelList(pageRequest, tsiUno, tsiKeyword) : searchResultRepository.getNoticeSelListEmptyKeyword(pageRequest, tsiUno);
            // return searchResultRepository.getNoticeSelList(pageRequest, tsiUno, percent, tsiKeyword);
        }
    }

    /*
    public Page<DefaultQueryDtoInterface> getNoticeList(Integer page, Integer tsiuno, Integer percent, String keyword, String tsiKeyword) {
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
        log.info("pageRequest: "+ pageRequest);
        log.info("tsiuno: "+tsiuno);

        if(tsiuno == 0) {
            return searchResultRepository.getNoticeList(pageRequest, percent);
        }else {
            return searchResultRepository.getNoticeSelList(pageRequest,tsiuno, percent, keyword);
        }
    }
    */

    public List<DefaultQueryDtoInterface> getNoticeListMain(Integer percent) {
        return searchResultRepository.getNoticeListMain(percent);
    }

/*
    public DefaultQueryDtoInterface getResultInfo(Integer tsrUno) {
        return searchResultRepository.getResultInfo(tsrUno);
    }
*/

    public DefaultQueryDtoInterface getInfoList(Integer tsiUno) {
        return searchResultRepository.getInfoList(tsiUno);
    }

    public Page<DefaultQueryDtoInterface> getTraceList(Integer page, String trkStatCd, String keyword) {
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        if (trkStatCd.equals("삭제 요청 중")) {
            trkStatCd = "20";
        } else if (trkStatCd.equals("관리 중")) {
            trkStatCd = "10";
        } else {
            trkStatCd = "";
        }
        return searchResultRepository.getTraceList(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, trkStatCd, keyword, pageRequest);
    }

/*
    public List<DefaultQueryDtoInterface> getTraceListByHome() {
        return searchResultRepository.getTraceListByHome(Consts.DATA_STAT_CD_NORMAL, Consts.TRK_STAT_CD_DEL_CMPL, "", "");
    }
*/

    public DefaultQueryDtoInterface getTraceInfo(Integer tsrUno) {
        return searchResultRepository.getTraceInfo(tsrUno);
    }

    public List<VideoInfoEntity> getVideoInfoList(Integer tsiUno) {
        return videoInfoRepository.findAllByTsiUno(tsiUno);
    }

    public Map<String, Object> getTraceHistoryList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryUserFileList(Integer page, String keyword) {
        log.info(" == getTraceHistoryUserFileList 진입 == ");
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceUserFileList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }



    public Map<String, Object> getTraceHistoryMonitoringList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringTsiUnoList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }
    
    // 대상자는 없고 대상자키워드 있을때
    public Map<String, Object> getTraceHistoryMonitoringUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryMonitoringUserFileList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }


    public Map<String, Object> getTraceHistoryDeleteReqList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqUserFileList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteReqTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteReqTsiUnoUserFileList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptUserFileList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> getTraceHistoryDeleteComptTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.getTraceHistoryDeleteComptTsiUnoUserFileList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }

    public Map<String, Object> allTimeMonitoringTsiUnoUserFileList(Integer page, String keyword, Integer tsiUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringTsiUnoUserFileList(keyword, pageRequest, tsiUno);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링

        return outMap;
    }


    public Map<String, Object> allTimeMonitoringList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링


        return outMap;
    }

    public Map<String, Object> allTimeMonitoringUserFileList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
//        Page<SearchResultEntity> traceHistoryListPage = searchResultRepository.findAllByTrkStatCdNotNullAndTsrTitleContainingOrderByTsrUnoDesc(keyword, pageRequest);

        Page<DefaultQueryDtoInterface> traceHistoryListPage = searchResultRepository.allTimeMonitoringUserFileList(keyword, pageRequest);

        outMap.put("traceHistoryList", traceHistoryListPage);
        outMap.put("totalPages", traceHistoryListPage.getTotalPages());
        outMap.put("number", traceHistoryListPage.getNumber());
        outMap.put("totalElements", traceHistoryListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        outMap.put("countMonitoring", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_MONITORING));  // 모니터링
        outMap.put("countDelReq", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_REQ));  // 삭제 요청
        outMap.put("countDelCmpl", searchResultRepository.countByTrkStatCdNotNullAndTrkStatCd(Consts.TRK_STAT_CD_DEL_CMPL));  // 삭제 완료
        outMap.put("allTimeMonitoringCnt", searchResultRepository.allTimeMonitoringCnt());                                    // 24시간 모니터링


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
        log.info("추적이력 일괄삭제 진입");
        List<SearchResultEntity> sre = searchResultRepository.findByTsrUnoIn(tsrUnoValues);
        // List<MatchResultEntity> mre = matchResultRepository.findByTsrUnoIn(tsrUnoValues);
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

    public void setMonitoringCd(int userUno,String userId, Integer tsrUno) {
        SearchResultEntity searchResultEntity = searchResultRepository.findByTsrUno(tsrUno);

        if(searchResultEntity.getMonitoringCd().equals("10")){ // 활성화 될 때
            searchResultEntity.setMonitoringCd(Consts.MONITORING_CD_NONE.equals(searchResultEntity.getMonitoringCd()) ? Consts.MONITORING_CD_ING : Consts.MONITORING_CD_NONE);
            searchResultRepository.save(searchResultEntity);

            AlltimeMonitoringHistEntity alltimeMonitoringHistEntity = new AlltimeMonitoringHistEntity();
            alltimeMonitoringHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            alltimeMonitoringHistEntity.setTsrUno(tsrUno);
            alltimeMonitoringHistEntity.setUserUno(userUno);
            alltimeMonitoringHistEntity.setUserId(userId);
            alltimeMonitoringHistEntity.setTamYn("Y");

            alltimeMonitoringHistRepository.save(alltimeMonitoringHistEntity);
        } else {
            searchResultEntity.setMonitoringCd(Consts.MONITORING_CD_NONE.equals(searchResultEntity.getMonitoringCd()) ? Consts.MONITORING_CD_ING : Consts.MONITORING_CD_NONE);
            searchResultRepository.save(searchResultEntity);

            AlltimeMonitoringHistEntity alltimeMonitoringHistEntity = new AlltimeMonitoringHistEntity();
            alltimeMonitoringHistEntity.setClkDmlDt(Timestamp.valueOf(LocalDateTime.now()));
            alltimeMonitoringHistEntity.setTsrUno(tsrUno);
            alltimeMonitoringHistEntity.setUserUno(userUno);
            alltimeMonitoringHistEntity.setUserId(userId);
            alltimeMonitoringHistEntity.setTamYn("N");

            alltimeMonitoringHistRepository.save(alltimeMonitoringHistEntity);
        }

    }

//    public Page<DefaultQueryDtoInterface> getNoticeList(Integer page, Integer tsrUno) {
//        Map<String, Object> outMap = new HashMap<>();
//        PageRequest pageRequest = PageRequest.of(page-1, Consts.PAGE_SIZE);
//        Page<SearchInfoEntity> noticeListPage ;
//        noticeListPage = searchInfoRepository.getNoticeListPage(pageRequest);
//
//        return outMap;
//    }

/*
    // admin 일 때
    public List<ResultCntQueryDtoInterface> getSearchInfoList(Integer page, String keyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, pageRequest);
        Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, pageRequest);

        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.getSearchInfoResultCnt("10","0", keyword, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);

        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }
*/

    public Map<String, Object> getSearchInfoList(Integer page, String keyword) {
        log.info("getSearchInfoList page: " + page);
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, pageRequest);
        //  Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, pageRequest);

        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getSearchInfoResultCnt("10","0", keyword, pageRequest);
        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }

/*
    public Page<List<ResultCntQueryDtoInterface>> getSearchInfoResultCnt(String dataType, String searchValue, String keyword) {
        return searchInfoRepository.getSearchInfoResultCnt("10","0", keyword);
    }
*/
/*
    // admin 아닐 때
    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, userUno, pageRequest);
        Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, userUno, pageRequest);

        outMap.put("searchInfoList", searchInfoListPage);
        outMap.put("totalPages", searchInfoListPage.getTotalPages());
        outMap.put("number", searchInfoListPage.getNumber());
        outMap.put("totalElements", searchInfoListPage.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;
    }
*/

    // admin 아닐 때
    public Map<String, Object> getSearchInfoList(Integer page, String keyword, Integer userUno) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10", keyword, userUno, pageRequest);
        // Page<SearchInfoEntity> searchInfoListPage = searchInfoRepository.findAllByDataStatCdAndSearchValueAndTsiKeywordContainingAndUserUnoAndTsrUnoIsNullOrderByTsiUnoDesc("10","0", keyword, userUno, pageRequest);
        Page<ResultCntQueryDtoInterface> searchInfoListPage = searchInfoRepository.getUserSearchInfoList("10","0", keyword, userUno, pageRequest);
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

/*
    public Map<Integer, String> getHistoryCount() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByUserUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getUserUno(), item.getUserId());
        }
        return userIdMap;
    }
*/


    public Map<Integer, String> getUserIdByTsiUnoMap() {
        List<UserIdDtoInterface> userIdList = searchInfoRepository.getUserIdByTsiUno();
        Map<Integer, String> userIdMap = new HashMap<>();

        for (UserIdDtoInterface item : userIdList) {
            userIdMap.put(item.getTsiUno(), item.getUserId());
        }

        return userIdMap;
    }

    public Map<Integer, String> getProgressPercentMap() {
        List<SearchJobRepository.ProgressPercentDtoInterface> progressPercentList = searchJobRepository.progressPercentByAll();
        Map<Integer, String> progressPercentMap = new HashMap<>();

        // progressPercentList => progressPercentMap
        for (SearchJobRepository.ProgressPercentDtoInterface item : progressPercentList) {
            progressPercentMap.put(item.getTsiUno(), String.format("%d%%", item.getProgressPercent()));
        }

        return progressPercentMap;
    }

    /**
     * 검색 결과 목록 조회
     *
     * @param monitoringCd (24시간 모니터링 코드 (10 : 안함, 20 : 모니터링))
     * @return List<SearchResultEntity> (검색 결과 엔티티 List)
     */

    /*
    public List<SearchResultEntity> findByMonitoringCd(String monitoringCd) {
        return searchResultRepository.findByMonitoringCd(monitoringCd);
    }
    */

    /**
     * 검색 결과 사이트 URL 목록 조회
     *
     * @param tsiUno (검색 정보 테이블의 key)
     * @return List<String> (검색 결과 사이트 URL List)
     */
    public List<String> findTsrSiteUrlDistinctByTsiUno(Integer tsiUno) {
        return searchResultRepository.findTsrSiteUrlDistinctByTsiUno(tsiUno);
    }

//    /**
//     * TODO : 재확산 자동추적
//     *
//     * @param tsjStatus      (일치율)
//     * @param optionalTsrUno (검색 결과 PK)
//     * @param page           (페이지)
//     * @param modelAndView   (ModelAndView)
//     */
    /*
    public void getNotice(String tsjStatus, Optional<Integer> optionalTsrUno, Integer page, ModelAndView modelAndView) {
        if (optionalTsrUno.isPresent()) {
            Optional<SearchInfoEntity> searchInfo = searchInfoRepository.findByTsrUno(optionalTsrUno.get());
            if (searchInfo.isPresent()) {
                modelAndView.addObject("searchInfo", searchInfo.get());
            }
        }

        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);
        *//*
        searchResultRepository.getResultInfoListOrderByTmrSimilarityDesc(tsiUno, keyword, tsjStatus1, tsjStatus2, tsjStatus3, tsjStatus4,
                snsStatus01, snsStatus02, snsStatus03, snsStatus04, pageRequest);
         *//*
    }
    */

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
        cell=row.createCell(0);
        cell.setCellValue("순번");

        cell=row.createCell(1);
        cell.setCellValue("사용자 아이디");

        cell=row.createCell(2);
        cell.setCellValue("검색타입");

        cell=row.createCell(3);
        cell.setCellValue("키워드");

        cell=row.createCell(4);
        cell.setCellValue("구글");

        cell=row.createCell(5);
        cell.setCellValue("페이스북");

        cell=row.createCell(6);
        cell.setCellValue("트위터");

        cell=row.createCell(7);
        cell.setCellValue("인스타그램");

        cell=row.createCell(8);
        cell.setCellValue("검색날짜");

        for(int i=0; i<searchHistoryExcelDtoList.size(); i++) {
            row=sheet.createRow(rowNum++);
            cell = row.createCell(0);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiUno());

            cell = row.createCell(1);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getUserId());

            cell = row.createCell(2);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTsiType());

            cell = row.createCell(3);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getKeyword());

            cell = row.createCell(4);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getGoogle());

            cell = row.createCell(5);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getFacebook());

            cell = row.createCell(6);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getTwitter());

            cell = row.createCell(7);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getInstagram());

            cell = row.createCell(8);
            cell.setCellValue(searchHistoryExcelDtoList.get(i).getFstDmlDt());

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
        cell=row.createCell(0);
        cell.setCellValue("검색이력번호");

        cell=row.createCell(1);
        cell.setCellValue("결과번호");

        cell=row.createCell(2);
        cell.setCellValue("SNS");

        cell=row.createCell(3);
        cell.setCellValue("키워드");

        cell=row.createCell(4);
        cell.setCellValue("제목");

        cell=row.createCell(5);
        cell.setCellValue("사이트 URL");

        cell=row.createCell(6);
        cell.setCellValue("일치율");

        cell=row.createCell(7);
        cell.setCellValue("사용자 아이디");

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

    public void CompletableFutureYandexByText(String url, String tsrSns, SearchInfoEntity insertResult) {

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchTextYandex(url, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByText::getOriginal
                                , Images_resultsByText::getThumbnail
                                , Images_resultsByText::getTitle
                                , Images_resultsByText::getLink
                                , Images_resultsByText::isFacebook
                                , Images_resultsByText::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenAccept((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
    }

    public void CompletableFutureYandexByImage(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try { // text기반 검색
                        return searchYandex(url, YandexByImageResult.class, YandexByImageResult::getError, YandexByImageResult::getInline_images);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try { // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByImage::getOriginal
                                , Images_resultsByImage::getThumbnail
                                , Images_resultsByImage::getTitle
                                , Images_resultsByImage::getSource
                                , Images_resultsByImage::isFacebook
                                , Images_resultsByImage::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApplyAsync((r) -> {
                    try { // 결과 db에 적재.
                        return saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }

    public void CompletableFutureYoutubeByResult(String url, String tsrSns, SearchInfoEntity insertResult) {
        // 이미지
        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchByYoutube(url, YoutubeByResult.class, YoutubeByResult::getError, YoutubeByResult::getVideo_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYoutube(
                                r
                                , tsrSns
                                , insertResult
                                , Youtube_resultsByText::getPosition_on_page
                                , Youtube_resultsByText::getLink
                                , Youtube_resultsByText::getTitle
                                , Youtube_resultsByText::getThumbnail
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApplyAsync((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        return saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                });
    }

    public void CompletableFutureText(String url, String tsrSns, SearchInfoEntity insertResult) {

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        // text기반 yandex 검색
                        return searchYandex(url, YandexByTextResult.class, YandexByTextResult::getError, YandexByTextResult::getImages_results);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenApply((r) -> {
                    try {
                        // 결과 저장.(이미지)
                        return saveYandex(
                                r
                                , tsrSns
                                , insertResult
                                , Images_resultsByText::getOriginal
                                , Images_resultsByText::getThumbnail
                                , Images_resultsByText::getTitle
                                , Images_resultsByText::getLink
                                , Images_resultsByText::isFacebook
                                , Images_resultsByText::isInstagram
                        );
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        return null;
                    }
                })
                .thenAccept((r) -> {
                    try {
                        // yandex검색을 통해 결과 db에 적재.
                        saveImgSearchYandex(r, insertResult);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });

    }

    public void searchYandexText(String tsrSns, SearchInfoEntity insertResult, SearchInfoDto searchInfoDto) {
        log.debug("searchYandexText 진입");
        int index=0;
        loop = true;

        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        do {
            String url = textYandexUrl
                    + "?engine=yandex"
                    + "&text=" + tsiKeywordHiddenValue
                    + "&lang=" + textYandexGl
                    + "&api_key=" + textYandexApikey
                    + "&p=" + index;

            log.info("url: "+url);
            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while(loop);

        index = 0;
        loop = true;

        do {
            String url = textYandexUrl
                    + "?engine=yandex"
                    + "&text=" + tsiKeywordHiddenValue
                    + "&lang=cn"
                    + "&api_key=" + textYandexApikey
                    + "&p=" + index;

            log.info("url: "+url);
            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while(loop);

        index = 0;
        loop = true;

        do {
            String url = textYandexUrl
                    + "?engine=yandex"
                    + "&text=" + tsiKeywordHiddenValue
                    + "&lang=kr"
                    + "&api_key=" + textYandexApikey
                    + "&p=" + index;

            log.info("url: "+url);
            CompletableFutureYandexByText(url, tsrSns, insertResult);

            if (index >= Integer.parseInt(textYandexCountLimit) - 1) {
                log.info("index: " + index);
                loop = false;
            }

            index++;
        } while(loop);

    }
/*

    public void searchYandexImage(String tsrSns, SearchInfoEntity insertResult) {
        String searchImageUrl = insertResult.getTsiImgPath() + insertResult.getTsiImgName();
        searchImageUrl = serverIp + searchImageUrl.substring(searchImageUrl.indexOf("/" + fileLocation3) + 1);
        // searchImageUrl = searchImageUrl.replace("172.20.7.100", "222.239.171.250");
        searchImageUrl = searchImageUrl.replace("172.30.1.220", "106.254.235.202");
        int index = 0;

        String url = textYandexUrl
                + "?engine=yandex_images"
                + "&lang=en"
                + "&api_key=" + textYandexApikey
                + "&p=" + index
                + "&url=" + searchImageUrl;

            CompletableFutureYandexByImage(url, tsrSns, insertResult);

    }
*/

    public void searchYandexYoutube(String tsrSns,SearchInfoEntity insertResult,SearchInfoDto searchInfoDto, String nationCode){
        log.info("========= searchYandexYoutube 진입 =========");
        String tsiKeywordHiddenValue = searchInfoDto.getTsiKeywordHiddenValue();

        try {
            String url = textYandexUrl
                    + "?engine=youtube"
                    + "&search_query=" + tsiKeywordHiddenValue
                    + "&gl=" + nationCode
                    + "&api_key=" + textYandexApikey;

            CompletableFutureYoutubeByResult(url, tsrSns, insertResult);
        } catch (Exception e){
            log.debug("Exception: "+e);
        }
    }

    public void deleteTsrUno(Integer tsrUno) {
        SearchResultEntity sre =  searchResultRepository.findByTsrUno(tsrUno);
        sre.setTrkStatCd(null);
        sre.setDataStatCd("20");
        searchResultRepository.save(sre);
    }

/*
    public List<NewKeywordEntity> getNewKeywordList() {
        List<NewKeywordEntity> nke = newKeywordRepository.findAll();

        for(int i=0; i<nke.size(); i++) {
            log.info("nke: "+ nke.get(i).getIdx());
            log.info("nke: " + nke.get(i).getKeyword());
        }
        return nke;
    }
*/

    public Map<String, Object> getUserSearchHistoryList(Integer page, String searchKeyword) {
        Map<String, Object> outMap = new HashMap<>();
        PageRequest pageRequest = PageRequest.of(page - 1, Consts.PAGE_SIZE);

        Page<UserSearchHistoryDtoInterface> userSearchHistoryList = searchInfoRepository.getUserSearchHistoryList(pageRequest, searchKeyword);

        outMap.put("userSearchHistoryList", userSearchHistoryList);
        outMap.put("totalPages", userSearchHistoryList.getTotalPages());
        outMap.put("number", userSearchHistoryList.getNumber());
        outMap.put("totalElements", userSearchHistoryList.getTotalElements());
        outMap.put("maxPage", Consts.MAX_PAGE);

        return outMap;

    }

    /*
    public FileOutputStream googleLensImageFile(String imageUrl, String randomFileName) throws IOException {
        URL url = new URL(imageUrl);

        String destinationFile = randomFileName;
        log.info("destinationFile: " + destinationFile);

        URLConnection connection = url.openConnection();

        // InputStream in = new BufferedInputStream(connection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);

*//*
        byte[] dataBuffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
*//*

        return fileOutputStream;
    }

    */

    public SearchInfoEntity insertSearchInfo(MultipartFile uploadFile, SearchInfoEntity param, String folder){
        boolean isFile = ! uploadFile.isEmpty();

        if(isFile){ // 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19: 영상
            try{
                InputStream inputStream = uploadFile.getInputStream();
                Tika tika = new Tika();
                String mimeType = tika.detect(inputStream);
                if(mimeType.substring(0,mimeType.indexOf("/")).contentEquals("video")){// 비디오
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
                    param.setTsiImgHeight(String.valueOf(bi.getHeight()));
                    param.setTsiImgWidth(String.valueOf(bi.getWidth()));
                    param.setTsiImgSize(String.valueOf(uploadFile.getSize() / 1024));
                    bi.flush();
                    if(! StringUtils.hasText(param.getTsiKeyword())){
                        param.setTsiType("17");
                    } else {
                        param.setTsiType("13");
                    }
                }

                String origName = uploadFile.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String extension = origName.substring(origName.lastIndexOf("."));

                String filePath = fileLocation1+folder;
                File destDir = new File(filePath);
                if(!destDir.exists()){
                    destDir.mkdirs();
                }

                uploadFile.transferTo(new File(destDir+File.separator+uuid+extension));

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

        return saveSearchInfo(param);
    }

    public void search(SearchInfoEntity param, SearchInfoDto siDto, String folder){
        try {
            List<NationCodeEntity> ncList = nationCodeRepository.findByNcIsActive(1);
            for (NationCodeEntity ncInfo : ncList) {
                // 검색 타입 11:키워드, 13:키워드+이미지, 15:키워드+영상, 17:이미지, 19:영상
                switch (param.getTsiType()) {
                    case CommonCode.searchTypeKeyword -> { // 11:키워드
                        if (param.getTsiGoogle() == 1) {
                            searchYandexYoutube(CommonCode.snsTypeGoogle, param, siDto, ncInfo.getNcCode().toLowerCase());
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle);
                        }
                        if (param.getTsiInstagram() == 1) {
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram);
                        }
                        if (param.getTsiFacebook() == 1) {
                            searchTextService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook);
                        }
                    }
                    case CommonCode.searchTypeKeywordImage -> { // 13:키워드+이미지
                            searchImageGoogleLensService.searchYandexByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase());
                            if (param.getTsiGoogle() == 1) {
                                searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeGoogle);
                            }
                            if (param.getTsiInstagram() == 1) {
                                searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeInstagram);
                            }
                            if (param.getTsiFacebook() == 1) {
                                searchTextImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase(), CommonCode.snsTypeFacebook);
                            }
                    }
                    case CommonCode.searchTypeKeywordVideo -> // 15:키워드+영상
                            search(param, folder, siDto);
                    case CommonCode.searchTypeImage -> { // 17:이미지
                        // searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder, searchInfoDto);
                        searchImageGoogleLensService.searchYandexByGoogleLensImage(CommonCode.snsTypeGoogle, param, ncInfo.getNcCode().toLowerCase());
                        searchImageService.search(param, siDto, ncInfo.getNcCode().toLowerCase());
                    }
                    case CommonCode.searchTypeVideo -> {// 19: 영상
                        search(param, folder, siDto);
                    }
                }
                // searchService.search(tsiGoogle, tsiFacebook, tsiInstagram, tsiTwitter, tsiType, insertResult, folder, searchInfoDto);
            }

            log.info("====== search 끝 ======");
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }
}
