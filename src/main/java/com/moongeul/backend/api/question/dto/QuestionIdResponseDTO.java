package com.moongeul.backend.api.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionIdResponseDTO {

    private Long questionId; // 생성된 Question id
}
