package com.nex.search.entity;

import java.sql.Timestamp;

public interface TraceDetailDtoInterface {
    Integer getTsrUno();
    Integer getTsiUno();
    String getTsrImgPath();
    String getTsrImgName();
    String getTsrSns();
    String getTsrDownloadUrl();
    String getTsrImgExt();
    String getTsrImgSize();
    String getTsrImgWidth();
    String getTsrImgHeight();
    Timestamp getFstDmlDt();
    String getTsiImgPath();
    String getTsiImgName();
    String getTsiImgExt();
    String getTsiType();
    String getTsiGoogle();
    String getTsiTwitter();
    String getTsiFacebook();
    String getTsiInstagram();
    String getTsiImgSize();
    String getTsiImgHeight();
    String getTsiImgWidth();
}
