package com.nex.user.entity;

public interface SearchHistoryExcelDto {
    String getTsiUno();
    String getUserId();
    String getTsiType(); // 검색타입
    String getKeyword(); // 키워드
    String getGoogle();
    String getFacebook();
    String getTwitter();
    String getInstagram();
    String getFstDmlDt(); // 검색 날짜
}
