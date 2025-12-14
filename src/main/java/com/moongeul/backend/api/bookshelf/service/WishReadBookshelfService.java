package com.moongeul.backend.api.bookshelf.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.bookshelf.entity.WishReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.WishReadBookshelfRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishReadBookshelfService {

    private final WishReadBookshelfRepository wishReadBookshelfRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    @Transactional
    public void addWishReadBook(String email, String isbn) {

        // 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 책 조회
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 이미 등록되어 있는지 확인
        if (wishReadBookshelfRepository.existsByMemberAndBook(member, book)) {
            throw new BadRequestException(ErrorStatus.BOOK_ALREADY_ADDED_EXCEPTION.getMessage());
        }

        // 읽고 싶은 책 등록
        WishReadBookshelf wishReadBookshelf = WishReadBookshelf.builder()
                .member(member)
                .book(book)
                .build();

        wishReadBookshelfRepository.save(wishReadBookshelf);
    }

    @Transactional
    public void removeWishReadBook(String email, String isbn) {

        // 회원 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 책 조회
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 읽고 싶은 책 삭제
        wishReadBookshelfRepository.deleteByMemberAndBook(member, book);
    }
}

