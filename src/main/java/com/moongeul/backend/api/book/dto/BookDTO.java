package com.moongeul.backend.api.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private String isbn; // ISBN
    private String title; // 책 제목
    private String author; // 저자
    private String bookImage; // 표지 이미지
    private String publisher; // 출판사
    private String description; // 책 소개
    private String pubdate; // 출판연도
    private Double ratingAverage; // 별점 평균
    private Integer ratingCount; // 별점 개수
}

