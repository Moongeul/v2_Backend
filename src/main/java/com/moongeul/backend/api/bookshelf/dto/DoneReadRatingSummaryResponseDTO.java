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
public class DoneReadRatingSummaryResponseDTO {
    private Long totalBooks; // 사용자가 기록한 총 책 수
    private List<DoneReadRatingRangeCountDTO> data; // 별점 구간별 기록 수
}
