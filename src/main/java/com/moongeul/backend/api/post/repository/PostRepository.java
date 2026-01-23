package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.ReadingTasteType;
import com.moongeul.backend.api.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 책의 게시글을 최신순으로 조회
    @Query("SELECT p FROM Post p WHERE p.book = :book ORDER BY p.createdAt DESC")
    Page<Post> findByBookOrderByCreatedAtDesc(@Param("book") Book book, Pageable pageable);

    // 내가 팔로우하는 사람들의 게시물 + 내 게시물 함께 조회 (서브쿼리 활용)
    @Query("SELECT p FROM Post p " +
            "WHERE p.member IN (SELECT f.following FROM Follow f WHERE f.follower = :member) " +
            "OR p.member = :member")
    Page<Post> findAllByFollower(@Param("member") Member member, Pageable pageable);

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
}
