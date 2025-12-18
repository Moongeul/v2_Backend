package com.moongeul.backend.api.bookshelf.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfItemDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
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
public class DoneReadBookshelfService {

    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public DoneReadBookshelfResponseDTO getDoneReadBooks(String email, Integer page, Integer size) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, size);

        // 읽은 책 목록 조회
        Page<DoneReadBookshelf> doneReadBookshelfPage = doneReadBookshelfRepository.findByMemberOrderByCreatedAtDesc(member, pageable);

        List<DoneReadBookshelfItemDTO> books = doneReadBookshelfPage.getContent().stream()
                .map(this::convertToItemDTO)
                .toList();

        // 페이지네이션 정보 계산
        int total = (int) doneReadBookshelfPage.getTotalElements();
        int totalPages = doneReadBookshelfPage.getTotalPages();
        boolean isLast = doneReadBookshelfPage.isLast();

        return DoneReadBookshelfResponseDTO.builder()
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .isLast(isLast)
                .books(books)
                .build();
    }

    private DoneReadBookshelfItemDTO convertToItemDTO(DoneReadBookshelf doneReadBookshelf) {
        Book book = doneReadBookshelf.getArticle().getBook();
        
        return DoneReadBookshelfItemDTO.builder()
                .articleId(doneReadBookshelf.getArticle().getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .bookImage(book.getBookImage())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .pubdate(book.getPubdate())
                .ratingAverage(book.getRatingAverage())
                .ratingCount(book.getRatingCount())
                .weight(doneReadBookshelf.getWeight())
                .height(doneReadBookshelf.getHeight())
                .build();
    }
}

