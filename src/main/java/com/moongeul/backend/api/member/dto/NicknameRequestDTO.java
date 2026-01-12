package com.moongeul.backend.api.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicknameRequestDTO {
    
    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 1, max = 12, message = "닉네임은 1자 이상 12자 이하여야 합니다")
    private String nickname;
}

