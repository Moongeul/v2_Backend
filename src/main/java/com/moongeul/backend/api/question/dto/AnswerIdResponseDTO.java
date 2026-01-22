package com.moongeul.backend.api.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerIdResponseDTO {
    private Long answerId; // 생성된 답변 ID
}
