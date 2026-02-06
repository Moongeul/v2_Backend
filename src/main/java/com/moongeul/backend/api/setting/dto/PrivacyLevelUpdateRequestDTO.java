package com.moongeul.backend.api.setting.dto;

import com.moongeul.backend.api.member.entity.PrivacyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacyLevelUpdateRequestDTO {

    @NotNull(message = "privacyLevel 값은 필수입니다.")
    private PrivacyLevel privacyLevel; // 계정 공개 범위
}
