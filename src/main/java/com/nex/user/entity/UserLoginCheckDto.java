package com.nex.user.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginCheckDto {

    @NotBlank(message = "아이디는 필수입니다.")
    private String loginId;
    @NotBlank(message = "비밀번호는 필수입니다.")
        private String loginPw;

    private Integer crawling_limit;
    private Integer percent_limit;
    private Long userUno;

    @Builder
    public UserLoginCheckDto(String loginId, String loginPw, Integer crawling_limit, Integer percent_limit, Long userUno) {
        this.loginId = loginId;
        this.loginPw = loginPw;
        this.crawling_limit = crawling_limit;
        this.percent_limit = percent_limit;
        this.userUno = userUno;
    }
}
