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
    private boolean isAdmin;
}
