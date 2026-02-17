package com.moongeul.backend.api.bookshelf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoneReadCalendarDayDTO {
    private Integer day; // 일자
    private Long postId; // 대표 기록 ID
    private String isbn; // 대표 도서 ISBN
    private String bookImage; // 대표 도서 표지
    private Integer count; // 해당 일자의 읽은 책 수
}
