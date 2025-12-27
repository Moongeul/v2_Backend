package com.moongeul.backend.api.bookshelf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoneReadBookshelfItemDTO {
    private Long articleId; // 게시글 ID
    private String isbn; // ISBN
    private String title; // 책 제목
    private Double ratingAverage; // 별점 평균
    private Integer ratingCount; // 별점 개수
    private Float weight; // 두께
    private Float height; // 높이
    private Integer postCount; // 해당 책에 대한 게시글 개수
}

