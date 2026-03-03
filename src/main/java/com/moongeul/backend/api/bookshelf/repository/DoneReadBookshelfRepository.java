package com.moongeul.backend.api.bookshelf.repository;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DoneReadBookshelfRepository extends JpaRepository<DoneReadBookshelf, Long> {
    @Query("SELECT d FROM DoneReadBookshelf d WHERE d.member = :member ORDER BY d.createdAt DESC")
    Page<DoneReadBookshelf> findByMemberOrderByCreatedAtDesc(@Param("member") Member member, Pageable pageable);
    
    @Query("SELECT d FROM DoneReadBookshelf d WHERE d.member = :member AND d.article.book = :book")
    Optional<DoneReadBookshelf> findByMemberAndBook(@Param("member") Member member, @Param("book") Book book);

    long countByMember(Member member);

    // 회원 탈퇴 시, 해당 회원의 읽은 책 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DoneReadBookshelf d WHERE d.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
