package com.moongeul.backend.api.bookshelf.repository;

import com.moongeul.backend.api.bookshelf.entity.WishReadBookshelf;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishReadBookshelfRepository extends JpaRepository<WishReadBookshelf, Long> {
    Optional<WishReadBookshelf> findByMemberAndBook(Member member, Book book);
    
    boolean existsByMemberAndBook(Member member, Book book);
    void deleteByMemberAndBook(Member member, Book book);
}

