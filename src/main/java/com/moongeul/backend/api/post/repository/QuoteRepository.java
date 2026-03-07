package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByPostId(Long postId);

    List<Quote> findAllByPostIdIn(List<Long> postIds);

    void deleteAllByPostId(Long postId);

    // 회원 탈퇴 시, 해당 회원이 작성한 모든 게시글의 인용구 삭제
    // Post 테이블과 조인해 해당 멤버의 게시글에 속한 인용구들만 타겟팅
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Quote q WHERE q.post.id IN (SELECT p.id FROM Post p WHERE p.member.id = :memberId)")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
