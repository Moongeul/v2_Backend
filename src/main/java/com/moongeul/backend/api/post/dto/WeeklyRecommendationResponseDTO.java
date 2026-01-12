package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.member.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyRecommendationResponseDTO {
    private Long postId; // 기록 ID
    private String bookImage; // 책 사진
    private String authorName; // 해당 기록을 작성한 사용자 이름
    private String profileImage; // 프로필 사진
    private Double rating; // 평점
    private String content; // 기록글
    private ReadingTasteType readingTasteType; // 로그인한 사용자의 책 취향
}

