package com.moongeul.backend.api.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDTO {
    private String nickname; // 사용자 닉네임
    private Integer rating; // 별점
    private String content; // 내용
}

