package com.moongeul.backend.api.book.service;

import com.moongeul.backend.api.book.dto.ReviewItemDTO;
import com.moongeul.backend.api.book.dto.ReviewRequestDTO;
import com.moongeul.backend.api.book.dto.ReviewResponseDTO;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.entity.Review;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.book.repository.ReviewRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    // 리뷰 작성
    @Transactional
    public void createReview(String isbn, ReviewRequestDTO reviewRequestDTO, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.findByMemberAndBook(member, book).isPresent()) {
            throw new BadRequestException(ErrorStatus.REVIEW_ALREADY_EXISTS_EXCEPTION.getMessage());
        }

        Review review = Review.builder()
                .rating(reviewRequestDTO.getRating())
                .content(reviewRequestDTO.getContent())
                .member(member)
                .book(book)
                .build();

        reviewRepository.save(review);
    }

    // 리뷰 조회 (최신순, 페이지네이션)
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviews(String isbn, Integer page, Integer size) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, size);

        // 리뷰 목록 조회 (최신순)
        Page<Review> reviewPage = reviewRepository.findByBookOrderByCreatedAtDesc(book, pageable);

        // 리뷰 평균 평점 계산
        Double averageRating = reviewRepository.calculateAverageRating(book);
        if (averageRating == null) {
            averageRating = 0.0;
        }

        List<ReviewItemDTO> reviewItems = reviewPage.getContent().stream()
                .map(this::convertToReviewItemDTO)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .reviewAverageRating(averageRating)
                .total(reviewPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(reviewPage.getTotalPages())
                .isLast(reviewPage.isLast())
                .data(reviewItems)
                .build();
    }

    // 리뷰 수정
    @Transactional
    public void updateReview(String isbn, ReviewRequestDTO reviewRequestDTO, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        Review review = reviewRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REVIEW_NOTFOUND_EXCEPTION.getMessage()));

        // 삭제하는 사람과 리뷰 주인이 같은지 확인
        if (!review.getMember().getEmail().equals(email)) {
            throw new BadRequestException(ErrorStatus.REVIEW_UNAUTHORIZED.getMessage());
        }

        // 리뷰 수정
        review.update(reviewRequestDTO.getRating(), reviewRequestDTO.getContent());
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(String isbn, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        Review review = reviewRepository.findByMemberAndBook(member, book)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REVIEW_NOTFOUND_EXCEPTION.getMessage()));

        // 삭제하는 사람과 리뷰 주인이 같은지 확인
        if (!review.getMember().getEmail().equals(email)) {
            throw new BadRequestException(ErrorStatus.REVIEW_UNAUTHORIZED.getMessage());
        }

        // 리뷰 삭제
        reviewRepository.delete(review);
    }

    private ReviewItemDTO convertToReviewItemDTO(Review review) {
        return ReviewItemDTO.builder()
                .nickname(review.getMember().getNickname())
                .rating(review.getRating())
                .content(review.getContent())
                .build();
    }
}

