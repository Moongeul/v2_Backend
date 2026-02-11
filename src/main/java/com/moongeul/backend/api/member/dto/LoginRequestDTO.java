package com.moongeul.backend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequestDTO {

    @NotBlank(message = "인가코드가 입력되지 않았습니다.")
    private String code; // 인가코드
    private String type; // local, deploy
}
