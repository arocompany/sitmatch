package com.nex.search.entity.dto;

import java.sql.Timestamp;

public interface DefaultQueryDtoInterface {
    Integer getTsrUno();
    Integer getTsiUno();
    String getTsrTitle();
    String getTsrSns();
    String getTsrSiteUrl();
    String getTsrImgPath();
    String getTsrImgName();
    String getTsrImgExt();
    String getTsrDownloadUrl();
    String getTsrImgHeight();
    String getTsrImgWidth();
    String getTsrImgSize();
    String getTrkStatCd();
    String getTrkHistMemo();
    String getTsrDataStatCd();
    Timestamp getTsrFstDmlDt();
    String getTsiType();
    String getTsiGoogle();
    String getTsiTwitter();
    String getTsiFacebook();
    String getTsiInstagram();
    String getTsiKeyword();
    String getTsiImgPath();
    String getTsiImgName();
    String getTsiImgExt();
    String getTsiImgHeight();
    String getTsiImgWidth();
    String getTsiImgSize();
    String getTsiStat();
    String getTsiDnaPath();
    String getTsiDnaText();
    String getTsiDataStatCd();
    String getTsiFstDmlDt();
    String getTsjStatus();
    String getTmrVScore();
    String getTmrTScore();
    String getTmrAScore();
    String getTmrStat();
    String getTmrMessage();
    String getTmrSimilarity();
    String getProgressPercent();
    String getTuUserId();
    String getMonitoringCd();

    String getMaxSimilarity();
    String getRe_monitor_cnt();

    String getTsi3tsiuno();
    String getTsi3Keyword();

    String getLastAlltimeHist();

    String getUserKeyword();
    String getTsiUserFile();
    String getTsiAlltimeMonitoring();

}