package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptFollowRequestDTO {

    private Long followerId; // 승인/삭제할 팔로워의 ID
    private String status; // 처리 상태 (승인: ACCEPT / 삭제: DELETE)
}
