package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyStoryResponseDTO {

    private Long total; // 전체 검색 결과 수
    private Integer page; // 현재 페이지
    private Integer size; // 페이지당 개수
    private Integer totalPages; // 전체 페이지 수
    private Boolean isLast; // 마지막 페이지 여부
    private List<StoryInfo> data; // 기록 리스트

    @Getter
    @Builder
    public static class StoryInfo {
        private Long storyId;
        private String storyImage;
        private LocalDateTime created; // 작성 시간
    }
}
