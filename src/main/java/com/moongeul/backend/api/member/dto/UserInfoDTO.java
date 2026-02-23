package com.moongeul.backend.api.member.dto;

import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoDTO {

    private final Long id;
    private final String name; // 회원 이름(실명)
    private final String profileImage; // 회원 이미지
    private final String nickname; //닉네임 (초기랜덤생성)
    private ReadingTasteType readingTasteType; // 독서 취향 유형
    private final Integer followerCount; // 팔로워 수
    private final Integer followingCount; // 팔로잉 수
    private final FollowStatus myFollowStatus; // 내가 해당 사용자를 팔로우했는지 여부
    private final PrivacyLevel privacyLevel; // 계정 공개 범위
}
