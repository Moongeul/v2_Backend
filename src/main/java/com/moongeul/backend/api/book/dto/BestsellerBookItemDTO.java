package com.moongeul.backend.api.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestsellerBookItemDTO {

    private String isbn; // ISBN
    private String bookImage; // 표지 이미지
    private String title; // 책 제목
    private String author; // 작가 이름
}
