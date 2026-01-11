package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 책의 게시글을 최신순으로 조회
    @Query("SELECT p FROM Post p WHERE p.book = :book ORDER BY p.createdAt DESC")
    Page<Post> findByBookOrderByCreatedAtDesc(@Param("book") Book book, Pageable pageable);

    // 내가 팔로우하는 사람들의 게시물 + 내 게시물 함께 조회 (서브쿼리 활용)
    @Query("SELECT p FROM Post p " +
            "WHERE p.member IN (SELECT f.following FROM Follow f WHERE f.follower = :member) " +
            "OR p.member = :member")
    Page<Post> findAllByFollower(@Param("member") Member member, Pageable pageable);
}
