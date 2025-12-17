package com.moongeul.backend.api.post.entity;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Quote> quotes = new ArrayList<>(); // 인상깊은구절

    @Enumerated(EnumType.STRING)
    private PostVisibility postVisibility; // 공개여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false)
    private Book book;

    public void addQuote(Quote quote) {
        quotes.add(quote);
    }

    public void addQuotes(List<Quote> quoteList) {
        quoteList.forEach(this::addQuote);
    }
}
