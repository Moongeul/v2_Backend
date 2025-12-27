package com.moongeul.backend.api.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {

    private BookInfo bookInfo; // 책 정보
    private double rating; // 별점
    private String content; // 감상평
    private List<QuoteDTO> quotes; // 인상깊은구절 리스트

    @Getter
    @Builder
    public static class BookInfo{

        private String isbn; // ISBN
        private String bookImage; // 표지 이미지
        private String title; // 책 제목
        private String author; // 저자
        private String publisher; // 출판사
    }

    @Getter
    @Builder
    public static class QuoteDTO {
        private String quoteContent; // 인용문 내용
        private Integer pageNumber; // 페이지 번호
    }
    
}
