package com.moongeul.backend.api.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccessTokenResponseDTO {

    // JSON의 access_token 필드를 이 변수에 매핑
    @JsonProperty("access_token")
    private String accessToken;
}
