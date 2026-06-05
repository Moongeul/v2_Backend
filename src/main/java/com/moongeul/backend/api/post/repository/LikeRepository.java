package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.LikeType;
import com.moongeul.backend.api.post.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    List<Likes> findByPostIdAndMemberId(Long postId, Long memberId);

    List<Likes> findAllByMemberEmailAndPostIdIn(String email, List<Long> postIds);

    Optional<Likes> findByPostIdAndMemberIdAndLikeType(Long postId, Long memberId, LikeType likeType);

    // 특정 게시글의 모든 공감 조회
    List<Likes> findByPostId(Long postId);

    // 회원 탈퇴 시 삭제
    // 1. 내가 누른 좋아요 삭제 (member_id 기준)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Likes l WHERE l.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    // 2. 내가 쓴 게시글에 대한 타인의 좋아요 삭제 (post의 member_id 기준)
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Likes l WHERE l.post.id IN (SELECT p.id FROM Post p WHERE p.member.id = :memberId)")
    void deleteByPostMemberId(@Param("memberId") Long memberId);
}
