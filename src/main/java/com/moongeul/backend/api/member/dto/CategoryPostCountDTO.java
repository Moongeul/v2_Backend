package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPostCountDTO {

    private Long categoryId; // 카테고리 ID
    private String categoryTitle; // 카테고리 이름
    private Integer postCount; // 해당 카테고리의 기록 갯수
}
