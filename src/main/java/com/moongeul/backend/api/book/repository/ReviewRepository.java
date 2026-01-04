package com.moongeul.backend.api.book.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.entity.Review;
import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // 회원과 책으로 리뷰 조회 (중복 체크용)
    Optional<Review> findByMemberAndBook(Member member, Book book);
    
    // 책의 리뷰를 최신순으로 조회
    @Query("SELECT r FROM Review r WHERE r.book = :book ORDER BY r.createdAt DESC")
    Page<Review> findByBookOrderByCreatedAtDesc(@Param("book") Book book, Pageable pageable);
    
    // 책의 리뷰 평균 평점 계산
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.book = :book")
    Double calculateAverageRating(@Param("book") Book book);
    
    // 책의 리뷰 개수
    Long countByBook(Book book);
}

