package com.moongeul.backend.api.bookshelf.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoneReadBookshelfRepository extends JpaRepository<DoneReadBookshelf, Long> {
    @Query("SELECT d FROM DoneReadBookshelf d WHERE d.member = :member ORDER BY d.createdAt DESC")
    Page<DoneReadBookshelf> findByMemberOrderByCreatedAtDesc(@Param("member") Member member, Pageable pageable);
    
    @Query("SELECT d FROM DoneReadBookshelf d WHERE d.member = :member AND d.article.book = :book")
    Optional<DoneReadBookshelf> findByMemberAndBook(@Param("member") Member member, @Param("book") Book book);
}

