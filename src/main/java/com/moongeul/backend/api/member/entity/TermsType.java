package com.moongeul.backend.api.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {

    SERVICE_TERMS_AGREE("서비스 이용약관 동의"),
    PRIVACY_POLICY_AGREE("개인 정보 수집 및 이용 동의"),
    MARKETING_AGREE("마케팅 정보 수신 동의");

    private final String key;
}
