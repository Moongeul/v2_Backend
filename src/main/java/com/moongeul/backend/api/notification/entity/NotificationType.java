package com.moongeul.backend.api.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    NOTICE("서버공지"),
    LIKE("공감 알림"),
    COMMENT("댓글 알림"),
    FOLLOW_OPEN("팔로우(공개 계정)"),
    FOLLOW_PRIVATE("팔로우(비공개 계정)"),
    FOLLOW_PRIVATE_ACCEPTED("팔로우(비공개 계정) - 승인됨");

    private final String key;
}
