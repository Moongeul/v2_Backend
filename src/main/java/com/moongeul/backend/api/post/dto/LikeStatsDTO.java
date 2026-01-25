package com.moongeul.backend.api.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatsDTO {

    private Map<String, Integer> likeTypeCount; // 공감 유형별 갯수
}
