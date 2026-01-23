package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatsResponseDTO {

    private Integer totalPostCount; // 전체 작성 갯수
    private List<CategoryPostCountDTO> data; // 카테고리별 기록 갯수
}
