package com.nex.common;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import com.nex.search.entity.dto.DefaultQueryDtoInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
@Slf4j
public class CommonStaticSearchUtil {
    public static <RESULT> SearchResultEntity getSearchResultGoogleReverseEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));
        sre.setTsrSearchValue(CommonCode.methodSerpApiTypeGoogleReverse);

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

    public static <RESULT> SearchResultEntity getSearchResultYandexReverseEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, Map<String, Object>> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result).get("link").toString());
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));
        sre.setTsrSearchValue(CommonCode.methodSerpApiTypeGoogleReverse);

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

    /**
     * 검색 작업 엔티티 추출
     *
     * @param sre (검색 결과 엔티티)
     * @return SearchJobEntity (검색 작업 엔티티)
     */
    public static SearchJobEntity getSearchJobEntity(SearchResultEntity sre) {
        SearchJobEntity sje = new SearchJobEntity();
        sje.setTsiUno(sre.getTsiUno());
        sje.setTsrUno(sre.getTsrUno());
        if (StringUtils.hasText(sre.getTsrImgPath())) {
            sje.setTsrImgPath(sre.getTsrImgPath().replaceAll("\\\\", "/"));
        } else {
            sje.setTsrImgPath(null);
        }
        sje.setTsrImgName(sre.getTsrImgName());
        sje.setTsrImgExt(sre.getTsrImgExt());
        sje.setTsjStatus(0);
        sje.setTsjStatusChild(0);
        sje.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        return sje;
    }

    /**
     * 검색 정보 엔티티 기본값 세팅
     *
     * @param sie (검색 정보 엔티티)
     */
    public static void setSearchInfoDefault(SearchInfoEntity sie) {
        sie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setTsiMonitoringCnt(0);
        sie.setDataStatCd("10");
        sie.setSearchValue("0");
        sie.setTsiIsDeploy(0);
        sie.setTsiCntTsr(0);
        sie.setTsiCntSimilarity(0);
        sie.setTsiCntChild(0);

        if(! StringUtils.hasText(sie.getTsiUserFile())){
            sie.setTsiUserFile(null);
            sie.setTsufUno(0);
        }
    }

    public static void setSearchInfoDefault_2(SearchInfoEntity sie) {
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setDataStatCd("10");
    }

    public static void setSearchResultDefault(SearchResultEntity sre) {
        sre.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setDataStatCd("10");
        sre.setMonitoringCd("10");
        sre.setTsrSimilarity(0);
        sre.setTsrTotalScore(0);
        if(StringUtils.hasText(sre.getTsrImgPath()))
            sre.setTsrState(0);
        else
            sre.setTsrState(1);
    }

    /**
     * 검색 작업 엔티티 기본값 세팅
     *
     * @param sje (검색 작업 엔티티)
     */
    public static void setSearchJobDefault(SearchJobEntity sje) {
        sje.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));

        if(StringUtils.hasText(sje.getTsrImgPath())) {
            sje.setTsjStatus(0);
            sje.setTsjStatusChild(0);
        }else{
            sje.setTsjStatus(10);
            sje.setTsjStatusChild(10);
        }
    }

    public static String generateRandomFileName(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder randomFileName = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomFileName.append(characters.charAt(index));
        }

        randomFileName.append(".jpg");
        log.info("randomFileName: " + randomFileName.toString());

        return randomFileName.toString();
    }

    public static <RESULT> SearchResultEntity getSearchResultGoogleLensEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) throws IOException {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        // sre.setTsrImgName(imageUrl);
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));
        sre.setTsrSearchValue(CommonCode.methodSerpApiTypeGoogleLens);

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

    public static <RESULT> SearchResultEntity getSearchResultTextEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));
        sre.setTsrSearchValue(CommonCode.methodSerpApiTypeETC);

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

    public static <RESULT> SearchResultEntity getSearchResultEntity2(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

    public static <RESULT> SearchResultEntity getSearchResultEntity3(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, Map<String, Object>> getOriginalMapFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn, Function<RESULT, Boolean> isTwitterFn) {
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalMapFn.apply(result).get("link").toString());
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(URLDecoder.decode(getLinkFn.apply(result)));

        // (Google: 11, Twitter: 13, Instagram:15, Facebook: 17)
        if (CommonCode.snsTypeFacebook.equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeFacebook);
        } else if(CommonCode.snsTypeTwitter.equals(tsrSns) && isTwitterFn.apply(result)){
            sre.setTsrSns(CommonCode.snsTypeTwitter);
        } else if (CommonCode.snsTypeInstagram.equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(CommonCode.snsTypeInstagram);
        } else {
            sre.setTsrSns(CommonCode.snsTypeGoogle);
        }

        return sre;
    }

//    public static String getSerpApiUrl(String url, String keyword, String country, String noCache, String location, Integer pageNo, String key, String imageUrl, String engine, String pageToken){
public static String getSerpApiUrl(String url, String keyword, String country, Integer pageNo, String imageUrl, String engine, String lensType){

        StringBuilder queryString = new StringBuilder();
        try {
//            if(StringUtils.hasText(noCache)) appendQueryParam(queryString, "no_cache", noCache);
//            if(StringUtils.hasText(location)) appendQueryParam(queryString, "location", location);
//            if(StringUtils.hasText(key)) appendQueryParam(queryString, "api_key", key);
            if(StringUtils.hasText(engine)) appendQueryParam(queryString, "engine", engine);
//            if(StringUtils.hasText(pageToken)) appendQueryParam(queryString, "page_token", pageToken);

            if(pageNo != null && pageNo > -1) appendQueryParam(queryString, "start", String.valueOf(pageNo * 10));

            if(StringUtils.hasText(keyword)) appendQueryParam(queryString, "q", keyword);

            if(StringUtils.hasText(country)){
                switch (country){
                    case "kr" -> appendQueryParam(queryString, "hl", "ko");
                    case "us" -> appendQueryParam(queryString, "hl", "en");
                    case "nl" -> appendQueryParam(queryString, "hl", "nl");
                    case "th" -> appendQueryParam(queryString, "hl", "th");
                    case "ru" -> appendQueryParam(queryString, "hl", "ru");
                    case "vn" -> appendQueryParam(queryString, "hl", "vi");
                    case "cn" -> appendQueryParam(queryString, "hl", "zh-cn");
                }
            }

            if(StringUtils.hasText(country)) appendQueryParam(queryString, "country", country);
            if(StringUtils.hasText(imageUrl)) appendQueryParam(queryString, "imageUrl", imageUrl);

            if(StringUtils.hasText(lensType)){
                switch (lensType){
                    case "emimage" -> appendQueryParam(queryString, "webtype", "emimage");
                    case "vmimage" -> appendQueryParam(queryString, "webtype", "vmimage");
                    case "about-this-image" -> appendQueryParam(queryString, "webtype", "about-this-image");
                }
            }

//            if(StringUtils.hasText(engine)) {
//                switch (engine) {
//                    case "google", "google_reverse_image", "google_images" -> {
//                        if(StringUtils.hasText(keyword)) appendQueryParam(queryString, "q", keyword);
//                        if(StringUtils.hasText(country)) appendQueryParam(queryString, "gl", country);
//                        if(StringUtils.hasText(imageUrl)) appendQueryParam(queryString, "image_url", imageUrl);
//                    }
//                    case "youtube" -> {
//                        if(StringUtils.hasText(keyword)) appendQueryParam(queryString, "search_query", keyword);
//                        if(StringUtils.hasText(country)) appendQueryParam(queryString, "gl", country);
//                        if(StringUtils.hasText(imageUrl)) appendQueryParam(queryString, "image_url", imageUrl);
//                    }
//                    case "google_lens" -> {
//                        if(StringUtils.hasText(country)) appendQueryParam(queryString, "country", country);
//                        if(StringUtils.hasText(imageUrl)) appendQueryParam(queryString, "url", imageUrl);
//                    }
//                    case "google_lens_image_sources" -> {
//                        if(StringUtils.hasText(country)) appendQueryParam(queryString, "country", country);
//                    }
//                }
//            }
//            appendQueryParam(queryString, "safe", "off");
//            appendQueryParam(queryString, "filter", "0");
//            appendQueryParam(queryString, "nfpr", "0");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url + "?" + queryString;

    }

    public static void appendQueryParam(StringBuilder queryString, String key, String value) throws UnsupportedEncodingException {
        if (queryString.length() > 0) {
            queryString.append("&");
        }
        if(! key.equals("q") && ! key.equals("search_query") && ! key.equals("url") && ! key.equals("imageUrl")) {
            queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()));
            queryString.append("=");
            queryString.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
        }else{
            queryString.append(key);
            queryString.append("=");
            queryString.append(value);
        }
    }

    public static void setOutMap(Map<String, Object> map, Page<DefaultQueryDtoInterface> page){
        map.put("traceHistoryList", page);
        map.put("totalPages", page.getTotalPages());
        map.put("number", page.getNumber());
        map.put("totalElements", page.getTotalElements());
        map.put("maxPage", Consts.MAX_PAGE);
    }
}