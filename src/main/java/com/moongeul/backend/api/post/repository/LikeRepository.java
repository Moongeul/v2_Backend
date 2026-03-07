package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.LikeType;
import com.moongeul.backend.api.post.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    List<Likes> findByPostIdAndMemberId(Long postId, Long memberId);

    List<Likes> findAllByMemberEmailAndPostIdIn(String email, List<Long> postIds);

    Optional<Likes> findByPostIdAndMemberIdAndLikeType(Long postId, Long memberId, LikeType likeType);

    // 특정 게시글의 모든 공감 조회
    List<Likes> findByPostId(Long postId);
}
