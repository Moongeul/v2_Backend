package com.moongeul.backend.api.bookshelf.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoneReadRatingRangeCountDTO {
    private String range; // 별점 구간 (예: 1.0~1.4)
    private Integer count; // 구간별 기록 수
}
