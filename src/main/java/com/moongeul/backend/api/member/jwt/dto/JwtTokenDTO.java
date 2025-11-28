package com.moongeul.backend.api.member.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JwtTokenDTO {
    private String grantType; //JWT에 대한 인증 타입
    private String accessToken;
    private String refreshToken;
}
