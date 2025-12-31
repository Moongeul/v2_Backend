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
public class PostAllResponseDTO {

    private Long total; // 전체 검색 결과 수
    private Integer page; // 현재 페이지
    private Integer size; // 페이지당 개수
    private Integer totalPages; // 전체 페이지 수
    private Boolean isLast; // 마지막 페이지 여부
    private List<PostDTO> data; // 기록 리스트
}
