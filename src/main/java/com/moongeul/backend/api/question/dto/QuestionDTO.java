package com.moongeul.backend.api.question.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private Long questionId; // 질문 ID
    private String content; // 질문 내용
    private Integer commentCnt; // 댓글 수
    private LocalDateTime createdAt; // 작성 시간
    private Boolean myArticle; // 내가 작성한 질문 인지 여부
    private BookInfo bookInfo; // 책 정보
    private Integer participantCount; // 총 참여 인원 수 (중복 제외, 질문 작성자 포함)
    private List<String> participantProfileImages; // 참여자 프로필 이미지 (최대 3명)

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookInfo {
        private String isbn; // ISBN
        private String bookImage; // 표지 이미지
        private String title; // 책 제목
        private String author; // 저자
        private String publisher; // 출판사
        private String pubdate; // 출판연도
        private Double ratingAverage; // 별점 평균
    }
}
