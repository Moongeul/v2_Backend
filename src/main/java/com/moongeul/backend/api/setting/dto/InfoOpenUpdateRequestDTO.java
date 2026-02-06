package com.moongeul.backend.api.setting.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoOpenUpdateRequestDTO {

    @NotNull(message = "isPublic 값은 필수입니다.")
    private Boolean isPublic; // 전체 공개

    @NotNull(message = "isFollowersOnly 값은 필수입니다.")
    private Boolean isFollowersOnly; // 팔로우한테만 공개

    @NotNull(message = "isPrivate 값은 필수입니다.")
    private Boolean isPrivate; // 비공개
}
