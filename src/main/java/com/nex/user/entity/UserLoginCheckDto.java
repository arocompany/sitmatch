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

    @Builder
    public UserLoginCheckDto(String loginId, String loginPw) {
        this.loginId = loginId;
        this.loginPw = loginPw;
    }
}
