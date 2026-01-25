package com.moongeul.backend.api.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteDTO {

    private String quoteContent; // 인상깊은 구절 내용
    private int pageNumber; // 페이지 번호
}
