package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private Long memberId;
    private String role;
    private String accessToken; // JWT Access Token (우리 서버)
    private String refreshToken; // JWT Refresh Token (우리 서버)
    private Boolean isReadingTaste; // 취향테스트 수행 여부
}
