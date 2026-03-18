package com.moongeul.backend.api.book.entity;

import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "book")
public class Book extends BaseTimeEntity {

    @Id
    private String isbn; // ISBN (PK)

    private String title; // 책 제목
    private String author; // 저자
    private String bookImage; // 표지
    private String publisher; // 출판사

    @Column(columnDefinition = "TEXT")
    private String description; // 책 소개
    private String pubdate; // 출판연도
    private Double ratingAverage; // 별점 평균
    private Integer ratingCount; // 별점 개수

    // 책 정보 업데이트
    public void update(String title, String author, String bookImage, String publisher, 
                      String description, String pubdate) {
        this.title = title;
        this.author = author;
        this.bookImage = bookImage;
        this.publisher = publisher;
        this.description = description;
        this.pubdate = pubdate;
    }

    public void updateRatingStats(Double ratingAverage, Integer ratingCount) {
        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
    }
}
