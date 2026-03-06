package com.moongeul.backend.api.post.repository;

import com.moongeul.backend.api.post.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByPostId(Long postId);

    List<Quote> findAllByPostIdIn(List<Long> postIds);

    void deleteAllByPostId(Long postId);
}
