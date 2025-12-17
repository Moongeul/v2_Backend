package com.moongeul.backend.api.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryListResponseDTO {

    private List<CategoryResponseDTO> categoryList; // 카테고리 리스트
}
