package com.moongeul.backend.api.readingTaste.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStatisticsResponseDTO {
    private long totalParticipants; // 전체 참여자 수
}
