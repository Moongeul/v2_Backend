package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostIdAndMemberId(Long postId, Long memberId);
}
