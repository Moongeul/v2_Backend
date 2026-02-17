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
public class DoneReadCalendarResponseDTO {
    private Integer year; // 조회 연도
    private Integer month; // 조회 월
    private List<DoneReadCalendarDayDTO> data; // 일자별 캘린더 데이터
}
