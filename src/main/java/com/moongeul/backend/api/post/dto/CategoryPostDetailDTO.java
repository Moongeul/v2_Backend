package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPostDetailDTO {

    private Long postId; // 기록 ID

    // 사용자 정보
    private String profileImage; // 사용자 프로필 이미지
    private String nickname; // 사용자 닉네임
    private ReadingTasteType readingTasteType; // 독서 취향

    // 기록 정보
    private LocalDateTime createdAt; // 작성일
    private String content; // 내용글
    private Double userRating; // 사용자가 남긴 별점
    private LocalDate readDate; // 읽은 날짜

    // 책 정보
    private String bookImage; // 책 사진
    private String bookTitle; // 책 이름
    private String publisher; // 출판사
    private Double bookRating; // 책 별점

    // 인상깊은 구절
    private List<QuoteDTO> quotes; // 인상깊은 구절 리스트

    // 공감 정보
    private LikeStatsDTO likeStats; // 공감 통계
}
