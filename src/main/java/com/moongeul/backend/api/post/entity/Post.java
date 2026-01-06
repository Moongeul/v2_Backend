package com.moongeul.backend.api.post.entity;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder // 빌더 패턴 사용을 위한 롬복 애너테이션
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Table(name = "POST") // 데이터베이스 테이블 이름 지정
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 id

    private LocalDate readDate; // 읽은날짜
    private Double rating; // 평점
    private Integer page; // 페이지 수
    private String content; // 감상평

    @Enumerated(EnumType.STRING)
    private PostVisibility postVisibility; // 공개여부

    // 공감 유형 별 개수 필드들
    @Column(nullable = false)
    private Integer relatableCount = 0; // 공감돼요
    @Column(nullable = false)
    private Integer sameTasteCount = 0; // 취향이 같아요
    @Column(nullable = false)
    private Integer impressiveExpressionCount = 0; // 표현이 인상적이에요
    @Column(nullable = false)
    private Integer wantToReadCount = 0; // 읽고싶네요
    @Column(nullable = false)
    private Integer helpfulCount = 0; // 도움이 됐어요


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false)
    private Book book;

    // 게시글 수정
    public void update(LocalDate readDate,
                       Double rating,
                       Integer page,
                       String content,
                       PostVisibility postVisibility,
                       Category category,
                       Book book) {
        this.readDate = readDate;
        this.rating = rating;
        this.page = page;
        this.content = content;
        this.postVisibility = postVisibility;
        this.category = category;
        this.book = book;
    }

    // 공감 개수 증가 메서드들
    public void incrementRelatableCount() { this.relatableCount++; }
    public void incrementSameTasteCount() { this.sameTasteCount++; }
    public void incrementImpressiveExpressionCount() { this.impressiveExpressionCount++; }
    public void incrementWantToReadCount() { this.wantToReadCount++; }
    public void incrementHelpfulCount() { this.helpfulCount++; }

    // 공감 개수 감소 메서드들
    public void decrementRelatableCount() {
        if (this.relatableCount > 0) this.relatableCount--;
    }
    public void decrementSameTasteCount() {
        if (this.sameTasteCount > 0) this.sameTasteCount--;
    }
    public void decrementImpressiveExpressionCount() {
        if (this.impressiveExpressionCount > 0) this.impressiveExpressionCount--;
    }
    public void decrementWantToReadCount() {
        if (this.wantToReadCount > 0) this.wantToReadCount--;
    }
    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) this.helpfulCount--;
    }
}
