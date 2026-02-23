package com.moongeul.backend.api.question.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {

    private Long answerId; // 댓글 ID
    private String content; // 댓글 내용
    private LocalDateTime createdAt; // 생성일자
    private Boolean myAnswer; // 내가 단 댓글인지 여부

    private MemberInfo memberInfo; // 댓글 작성자 정보

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberInfo {
        private String profileImage; // 프로필 이미지
        private String nickname; // 닉네임
        private ReadingTasteType readingTasteType; // 독서 취향 유형
    }
}
