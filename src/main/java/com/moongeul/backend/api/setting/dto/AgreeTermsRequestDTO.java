package com.moongeul.backend.api.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreeTermsRequestDTO {

    private boolean serviceTermsAgree;
    private boolean privatePolicyAgree;
    private boolean marketingAgree;
}
