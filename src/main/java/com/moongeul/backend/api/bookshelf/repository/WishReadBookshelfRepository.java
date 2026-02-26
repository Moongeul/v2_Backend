package com.moongeul.backend.api.bookshelf.repository;

import com.moongeul.backend.api.bookshelf.entity.WishReadBookshelf;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WishReadBookshelfRepository extends JpaRepository<WishReadBookshelf, Long> {
    Optional<WishReadBookshelf> findByMemberAndBook(Member member, Book book);
    
    boolean existsByMemberAndBook(Member member, Book book);
    void deleteByMemberAndBook(Member member, Book book);
    
    @Query("SELECT w FROM WishReadBookshelf w WHERE w.member = :member ORDER BY w.createdAt DESC")
    Page<WishReadBookshelf> findByMemberOrderByCreatedAtDesc(@Param("member") Member member, Pageable pageable);

    // 회원 탈퇴 시, 해당 회원의 읽고싶은 책 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM WishReadBookshelf w WHERE w.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}

