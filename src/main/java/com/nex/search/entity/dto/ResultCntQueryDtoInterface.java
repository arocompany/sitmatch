package com.nex.search.entity.dto;

import com.nex.search.entity.SearchInfoParamsEntity;
import com.nex.search.entity.VideoInfoEntity;

import java.sql.Timestamp;
import java.util.List;

public interface ResultCntQueryDtoInterface {
    Integer getTsrUno();
    Integer getTsiUno();
    Integer getTsiUserUno();
    String getTsiDataStatCd();
    Timestamp getTsiFstDmlDt();
    Timestamp getTsiLstDmlDt();
    String getTsiDnaPath();
    String getTsiDnaText();
    String getTsiGoogle();
    String getTsiTwitter();
    String getTsiFacebook();
    String getTsiInstagram();
    String getTsiImgExt();
    String getTsiImgHeight();
    String getTsiImgPath();
    String getTsiImgName();
    String getTsiImgRealPath();
    String getTsiImgWidth();
    String getTsiImgSize();
    String getTsiKeyword();
    String getTsiStat();
    String getTsiType();
    String getResultCnt();
    String getTmrSimilarityCnt();

    SearchInfoParamsEntity getParams();
    Integer getTsiIsNationUs();
    Integer getTsiIsNationCn();
    Integer getTsiIsNationKr();
    Integer getTsiIsNationNl();
    Integer getTsiIsNationTh();
    Integer getTsiIsNationRu();
    Integer getTsiIsNationVn();
    Integer getTsiIsEngineGoogle();
    Integer getTsiIsEngineYoutube();
    Integer getTsiIsEngineGoogleReverseImage();
    Integer getTsiIsEngineGoogleLens();
    Integer getTsiIsEngineBaidu();
    Integer getTsiIsEngineBing();
    Integer getTsiIsEngineDuckduckgo();
    Integer getTsiIsEngineYahoo();
    Integer getTsiIsEngineYandex();
    Integer getTsiIsEngineYandexImage();
    Integer getTsiIsEngineNaver();

    String getTsiSearchType();
    String getTsiUserFile();

    List<VideoInfoEntity> getVideoList();
    void setVideoList(List<VideoInfoEntity> list);
}