package com.nex.common;

import com.nex.search.entity.SearchInfoEntity;
import com.nex.search.entity.SearchJobEntity;
import com.nex.search.entity.SearchResultEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.function.Function;
@Slf4j
public class CommonStaticSearchUtil {
    public static <RESULT> SearchResultEntity getSearchResultGoogleReverseEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) {
        log.info("searchResultEntity: "+getTitleFn+" getLinkFn: " + getLinkFn);
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));
        sre.setTsrSearchValue("0");

        log.info("setTsrSiteUrl: " + getLinkFn.apply(result));
        //sre.setTsrSns("11");

        //Facebook 검색이고, source 값이 Facebook 인 경우
        if ("17".equals(tsrSns) && isFacebookFn.apply(result)) {
            sre.setTsrSns(tsrSns);
        } else if ("15".equals(tsrSns) && isInstagramFn.apply(result)) {
            sre.setTsrSns(tsrSns);
        } else {
            sre.setTsrSns("11");
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
            sje.setTsrImgPath("");
        }
        sje.setTsrImgName(sre.getTsrImgName());
        sje.setTsrImgExt(sre.getTsrImgExt());
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
        // sie.setTsiAlltimeMonitoring(String.valueOf(Timestamp.valueOf(LocalDateTime.now())+"   "));
        sie.setDataStatCd("10");
        sie.setSearchValue("0");
    }

    public static void setSearchInfoDefault_2(SearchInfoEntity sie) {
        // sie.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sie.setDataStatCd("10");
    }

    public static void setSearchResultDefault(SearchResultEntity sre) {
        sre.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sre.setDataStatCd("10");
        sre.setMonitoringCd("10");
    }

    /**
     * 검색 작업 엔티티 기본값 세팅
     *
     * @param sje (검색 작업 엔티티)
     */
    public static void setSearchJobDefault(SearchJobEntity sje) {
        sje.setFstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setLstDmlDt(Timestamp.valueOf(LocalDateTime.now()));
        sje.setTsjStatus("00");
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
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) throws IOException {
        // log.info("searchResultEntity: "+getTitleFn+" getLinkFn: " + getLinkFn);

        /*
        String imageUrl = getOriginalFn.apply(result);
        log.info("getSearchResultGoogleLensEntity imageUrl: "+imageUrl);

        try {
            imageUrl = googleLensImageFile(imageUrl).toString();
        } catch (Exception e){
            e.printStackTrace();
        }

        log.info("imageUrl: " + imageUrl);
*/

        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        // sre.setTsrImgName(imageUrl);
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));
        sre.setTsrSearchValue("1");

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

    public static <RESULT> SearchResultEntity getSearchResultTextEntity(int tsiUno, String tsrSns, RESULT result
            , Function<RESULT, String> getOriginalFn, Function<RESULT, String> getTitleFn, Function<RESULT, String> getLinkFn
            , Function<RESULT, Boolean> isFacebookFn, Function<RESULT, Boolean> isInstagramFn) {
        log.info("searchResultEntity: "+getTitleFn+" getLinkFn: " + getLinkFn);
        SearchResultEntity sre = new SearchResultEntity();
        sre.setTsiUno(tsiUno);
        sre.setTsrJson(result.toString());
        sre.setTsrDownloadUrl(getOriginalFn.apply(result));
        sre.setTsrTitle(getTitleFn.apply(result));
        sre.setTsrSiteUrl(getLinkFn.apply(result));
        sre.setTsrSearchValue("2");

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

    public static String getSerpApiUrlForGoogle(String url, String keyword, String country, String noCache, String location, Integer pageNo, String key){

        StringBuilder queryString = new StringBuilder();
        try {

            appendQueryParam(queryString, "q", keyword);
            appendQueryParam(queryString, "gl", country);
            appendQueryParam(queryString, "no_cache", noCache);
            appendQueryParam(queryString, "location", location);
            appendQueryParam(queryString, "start", String.valueOf(pageNo * 10));
            appendQueryParam(queryString, "api_key", key);
            appendQueryParam(queryString, "safe", "off");
            appendQueryParam(queryString, "filter", "0");
            appendQueryParam(queryString, "nfpr", "0");
            appendQueryParam(queryString, "engine", "google");
        } catch (UnsupportedEncodingException e) {
            // 예외 처리 로직 추가
            e.printStackTrace();
        }
        return url + "?" + queryString;

    }

    public static void appendQueryParam(StringBuilder queryString, String key, String value) throws UnsupportedEncodingException {
        if (queryString.length() > 0) {
            queryString.append("&");
        }
        queryString.append(URLEncoder.encode(key, StandardCharsets.UTF_8.toString()));
        queryString.append("=");
        queryString.append(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
    }
}
