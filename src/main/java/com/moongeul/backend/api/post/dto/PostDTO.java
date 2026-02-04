package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.member.entity.ReadingTasteType;
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
public class PostDTO {

    private Long postId; // 게시글 id

    private MemberInfo memberInfo; // 사용자 정보
    private LocalDateTime created; // 작성 시간

    private BookInfo bookInfo; // 책 정보

    private Double rating; // 별점
    private String content; // 감상평
    private LocalDate readDate; // 읽은날짜

    private Integer quotesCnt; // 인용 총 개수
    private List<QuoteDTO> quotes; // 인상깊은구절 리스트

    private LikesInfo likesInfo; // 공감 개수 정보

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String nickname; // 닉네임
        private String profileImage; // 회원 이미지
        private ReadingTasteType readingTasteType; // 독서 취향 유형
    }

    @Getter
    @Builder
    public static class BookInfo{

        private String isbn; // ISBN
        private String bookImage; // 표지 이미지
        private String title; // 책 제목
        private String author; // 저자
        private String publisher; // 출판사
        private String pubdate; // 출판연도
        private Double ratingAverage; // 별점 평균
    }

    @Getter
    @Builder
    public static class QuoteDTO {
        private String quoteContent; // 인용문 내용
        private Integer pageNumber; // 페이지 번호
    }

    @Getter
    @Builder
    public static class LikesInfo{
        private Integer relatableCount; // 공감돼요
        private Integer sameTasteCount; // 취향이 같아요
        private Integer impressiveExpressionCount; // 표현이 인상적이에요
        private Integer wantToReadCount; // 읽고싶네요
        private Integer helpfulCount; // 도움이 됐어요
    }
    
}
