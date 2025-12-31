package com.moongeul.backend.api.question.dto;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.question.entity.Question;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequestDTO {

    @NotBlank(message = "ISBN은 필수입니다")
    private String isbn; // 책

    @NotBlank(message = "질문 작성은 필수입니다")
    private String content; // 질문

    public Question toEntity(Member member, Book book){
        return Question.builder()
                .content(this.content)
                .commentCnt(0)
                .member(member)
                .book(book)
                .build();
    }
}
