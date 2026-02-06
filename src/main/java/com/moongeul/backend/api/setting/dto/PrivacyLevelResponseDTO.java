package com.moongeul.backend.api.setting.dto;

import com.moongeul.backend.api.member.entity.PrivacyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacyLevelResponseDTO {

    private PrivacyLevel privacyLevel; // 계정 공개 범위
}
