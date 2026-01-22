package com.moongeul.backend.api.question.dto;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.question.entity.Answer;
import com.moongeul.backend.api.question.entity.Question;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerCreateRequestDTO {

    @NotNull(message = "질문 ID는 필수입니다")
    private Long questionId; // 질문 ID

    @NotBlank(message = "답변 내용은 필수입니다")
    private String content; // 답변 내용

    public Answer toEntity(Member member, Question question) {
        return Answer.builder()
                .content(this.content)
                .question(question)
                .member(member)
                .build();
    }
}
