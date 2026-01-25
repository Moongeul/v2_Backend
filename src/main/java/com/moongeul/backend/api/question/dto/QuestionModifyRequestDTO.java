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
public class QuestionModifyRequestDTO {

    @NotBlank(message = "ISBN은 필수입니다")
    private String isbn; // 수정할 책 ISBN

    @NotBlank(message = "질문 내용은 필수입니다")
    private String content; // 수정할 질문 내용
}
