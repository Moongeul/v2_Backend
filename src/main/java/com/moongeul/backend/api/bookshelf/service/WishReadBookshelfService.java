package com.moongeul.backend.api.bookshelf.service;

import com.moongeul.backend.api.book.dto.BookDTO;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.bookshelf.dto.WishReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.entity.WishReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.WishReadBookshelfRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishReadBookshelfService {

    private final WishReadBookshelfRepository wishReadBookshelfRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    // 읽고 싶은 책장 등록
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

    // 읽고 싶은 책장 전체 조회
    @Transactional(readOnly = true)
    public WishReadBookshelfResponseDTO getWishReadBooks(String email, Integer page, Integer size) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // 읽고 싶은 책 목록 조회
        Page<WishReadBookshelf> wishReadBookshelfPage = wishReadBookshelfRepository.findByMemberOrderByCreatedAtDesc(member, pageable);

        List<BookDTO> books = wishReadBookshelfPage.getContent().stream()
                .map(wishReadBookshelf -> convertToBookDTO(wishReadBookshelf.getBook()))
                .toList();

        // 페이지네이션 정보 계산
        int total = (int) wishReadBookshelfPage.getTotalElements();
        int totalPages = wishReadBookshelfPage.getTotalPages();
        boolean isLast = wishReadBookshelfPage.isLast();

        return WishReadBookshelfResponseDTO.builder()
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .isLast(isLast)
                .books(books)
                .build();
    }

    private BookDTO convertToBookDTO(Book book) {
        return BookDTO.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .ratingAverage(book.getRatingAverage())
                .ratingCount(book.getRatingCount())
                .build();
    }
}

