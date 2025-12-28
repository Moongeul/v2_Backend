package com.moongeul.backend.api.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseDTO {

    private String readingTasteType; // 독서 취향 유형
    private String intro; // 한줄 소개
}
