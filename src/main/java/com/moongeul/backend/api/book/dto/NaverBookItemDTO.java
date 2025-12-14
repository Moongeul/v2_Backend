package com.moongeul.backend.api.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NaverBookItemDTO {
    @JsonProperty("title")
    private String title; // 책 제목

    @JsonProperty("link")
    private String link; // 네이버 도서 정보 URL

    @JsonProperty("image")
    private String image; // 표지 이미지 URL

    @JsonProperty("author")
    private String author; // 저자

    @JsonProperty("discount")
    private String discount; // 할인가격

    @JsonProperty("publisher")
    private String publisher; // 출판사

    @JsonProperty("pubdate")
    private String pubdate; // 출판연도

    @JsonProperty("isbn")
    private String isbn; // ISBN

    @JsonProperty("description")
    private String description; // 책 소개
}

