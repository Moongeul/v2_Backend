package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.ReadingTasteType;
import com.moongeul.backend.api.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 비회원용: PUBLIC 게시글만 조회
    @Query("SELECT p FROM Post p WHERE p.postVisibility = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findAllForAnonymousUsers(Pageable pageable);

    // 회원용: PUBLIC + (FOLLOWERS & 팔로우 중인 유저) + 내 게시글
    @Query("SELECT p FROM Post p " +
            "WHERE p.postVisibility = 'PUBLIC' " +
            "OR (p.postVisibility = 'FOLLOWERS' AND p.member IN (SELECT f.following FROM Follow f WHERE f.follower.email = :email)) " +
            "OR p.member.email = :email " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findAllForMember(@Param("email") String email, Pageable pageable);

    // FOLLOWERS & 팔로우 중인 유저 + 내 게시글
    @Query("SELECT p FROM Post p " +
            "WHERE p.postVisibility = 'FOLLOWERS' AND p.member IN (SELECT f.following FROM Follow f WHERE f.follower.email = :email) " +
            "OR p.member.email = :email " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findAllByFollower(@Param("email") String email, Pageable pageable);

    // 책의 게시글을 최신순으로 조회
    @Query("SELECT p FROM Post p WHERE p.book = :book ORDER BY p.createdAt DESC")
    Page<Post> findByBookOrderByCreatedAtDesc(@Param("book") Book book, Pageable pageable);

    // 같은 취향 사용자들의 기록 중 이번 주 가장 공감을 많이 받은 기록 조회
    // 모든 공감 유형의 합계(relatableCount + sameTasteCount + impressiveExpressionCount + wantToReadCount + helpfulCount) 기준으로 정렬
    @Query("SELECT p FROM Post p " +
            "WHERE p.member.readingTasteType = :readingTasteType " +
            "AND p.createdAt >= :weekStart " +
            "ORDER BY (p.relatableCount + p.sameTasteCount + p.impressiveExpressionCount + p.wantToReadCount + p.helpfulCount) DESC, p.createdAt DESC")
    List<Post> findWeeklyRecommendationByReadingTasteType(
            @Param("readingTasteType") ReadingTasteType readingTasteType,
            @Param("weekStart") LocalDateTime weekStart);

    // 특정 사용자의 전체 기록 수 조회
    long countByMemberId(Long memberId);

    // 특정 사용자의 카테고리별 기록 수 조회
    long countByMemberIdAndCategoryId(Long memberId, Long categoryId);

    // 카테고리별 기록 조회 (최신순)
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId ORDER BY p.createdAt DESC")
    Page<Post> findByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    // 카테고리별 기록 조회 (오래된순)
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId ORDER BY p.createdAt ASC")
    Page<Post> findByCategoryIdOrderByCreatedAtAsc(@Param("categoryId") Long categoryId, Pageable pageable);

    // 카테고리별 기록 조회 (평점 높은순)
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId ORDER BY p.rating DESC, p.createdAt DESC")
    Page<Post> findByCategoryIdOrderByRatingDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    // 카테고리별 기록 조회 (평점 낮은순)
    @Query("SELECT p FROM Post p WHERE p.category.id = :categoryId ORDER BY p.rating ASC, p.createdAt DESC")
    Page<Post> findByCategoryIdOrderByRatingAsc(@Param("categoryId") Long categoryId, Pageable pageable);
}
