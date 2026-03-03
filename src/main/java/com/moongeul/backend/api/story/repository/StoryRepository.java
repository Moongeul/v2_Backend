package com.moongeul.backend.api.story.repository;

import com.moongeul.backend.api.story.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface StoryRepository extends JpaRepository<Story, Long> {

    // 1. 전체 공개 스토리 조회 (비로그인/로그인 공용) (24시간 이내 + 팔로잉 스토리 + 내 스토리 포함)
    @Query("SELECT DISTINCT s FROM Story s " +
            "JOIN FETCH s.member m " +
            "JOIN FETCH s.post p " +
            "LEFT JOIN Follow f ON f.following = m AND f.follower.email = :email " +
            "WHERE s.createdAt >= :timeLimit " +
            "AND (" +
            "    p.postVisibility = 'PUBLIC' " +          // 1. 전체 공개 스토리
            "    OR m.email = :email " +                  // 2. 내 스토리
            "    OR (p.postVisibility = 'FOLLOWERS' AND f.followStatus = 'ACCEPTED')" + // 3. 팔로잉 중인 스토리
            ")")
    Page<Story> findAllPublicStories(
            @Param("email") String email,
            @Param("timeLimit") LocalDateTime timeLimit,
            Pageable pageable
    );

    // 2. 팔로워 공개 스토리 조회 (24시간 이내 + 팔로잉 스토리 + 내 스토리)
    @Query("SELECT s FROM Story s " +
            "JOIN FETCH s.member " +
            "JOIN FETCH s.post p " +
            "LEFT JOIN Follow f ON f.following = s.member AND f.follower.email = :email " +
            "WHERE ((s.member.email = :email) " +
            "OR (p.postVisibility = 'FOLLOWERS' AND f.followStatus = 'ACCEPTED')) " +
            "AND s.createdAt >= :timeLimit")
    Page<Story> findAllFollowerStories(
            @Param("email") String email,
            @Param("timeLimit") LocalDateTime timeLimit,
            Pageable pageable
    );
}
