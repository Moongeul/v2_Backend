package com.moongeul.backend.api.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchRequestDTO {
    @NotBlank(message = "검색어는 필수입니다.")
    private String query; // 검색어

    @Min(value = 1, message = "페이지는 1 이상이어야 합니다.")
    private Integer page = 1; // 페이지 번호 (기본값 1)

    @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.")
    private Integer size = 10; // 한 페이지당 개수 (기본값 10, 최대 100)
}


