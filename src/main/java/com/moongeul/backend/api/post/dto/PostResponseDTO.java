package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.book.dto.BookDTO;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostResponseDTO {

    // 필수
    private BookDTO bookDTO; // 필요 - 책 제목/작가/출판사
    private double rating;
    
    // 선택
    private String content; // 감상평
    private List<String> quotes; // 인상깊은구절 리스트
    
}
