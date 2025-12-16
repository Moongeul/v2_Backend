package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
