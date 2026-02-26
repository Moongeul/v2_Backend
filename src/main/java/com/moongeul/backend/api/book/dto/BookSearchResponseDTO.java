package com.moongeul.backend.api.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponseDTO {
    private String type; // 검색 타입 (book, user, all)
    private Integer total; // 전체 검색 결과 수
    private Integer page; // 현재 페이지
    private Integer size; // 페이지당 개수
    private Integer totalPages; // 전체 페이지 수
    private Boolean isLast; // 마지막 페이지 여부
    private List<BookDTO> bookData; // 책 목록
    private List<BookSearchUserDTO> userData; // 사용자 목록
}
