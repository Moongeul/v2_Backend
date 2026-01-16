package com.moongeul.backend.api.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrivacyLevel {

    PUBLIC("전체공개"),
    FOLLOWER_ONLY("팔로워에게만 일부 공개"),
    PRIVATE("비공개");

    private final String key;
}
