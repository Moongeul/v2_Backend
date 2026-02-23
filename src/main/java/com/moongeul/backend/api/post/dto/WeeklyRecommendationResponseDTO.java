package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
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
    private String bookTitle; // 책 제목
    private String isbn; // 책 ISBN
    private String author; // 책 저자
    private String publisher; // 출판사
    private String pubdate; // 출판년도
    private Double bookRating; // 책 별점
    private Double rating; // 평점
    private String content; // 기록글
    private ReadingTasteType readingTasteType; // 로그인한 사용자의 책 취향
}
