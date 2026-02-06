package com.moongeul.backend.api.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoOpenResponseDTO {

    private Boolean isPublic; // 전체 공개
    private Boolean isFollowersOnly; // 팔로우한테만 공개
    private Boolean isPrivate; // 비공개
}
