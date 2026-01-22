package com.moongeul.backend.api.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerModifyRequestDTO {

    @NotBlank(message = "답변 내용은 필수입니다")
    private String content; // 수정할 답변 내용
}
