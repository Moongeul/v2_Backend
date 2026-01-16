package com.moongeul.backend.api.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FollowStatus {

    NONE("팔로우 아님"),
    PENDING("요청 대기중"),
    ACCEPTED("팔로우 완료");

    private final String key;
}
