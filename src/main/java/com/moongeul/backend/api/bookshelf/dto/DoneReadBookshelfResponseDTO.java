package com.moongeul.backend.api.bookshelf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoneReadBookshelfResponseDTO {
    private Integer total; // 전체 개수
    private Integer page; // 현재 페이지
    private Integer size; // 페이지당 개수
    private Integer totalPages; // 전체 페이지 수
    private Boolean isLast; // 마지막 페이지 여부
    private List<DoneReadBookshelfItemDTO> data; // 읽은 책 목록
}

