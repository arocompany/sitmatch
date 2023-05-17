package com.nex.user.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SessionInfoDto {
    private Integer userUno;
    private String userId;
    private String userNm;
    private Integer crawling_limit; // 웹 크롤링 뎁스 설정
    private Integer percent_limit;  // 유사도 가중치 설정
    private boolean isAdmin;
}
