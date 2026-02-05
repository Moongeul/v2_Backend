package com.moongeul.backend.api.book.service;

import com.moongeul.backend.api.book.dto.ReviewItemDTO;
import com.moongeul.backend.api.book.dto.ReviewResponseDTO;
import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.post.dto.PostDTO;
import com.moongeul.backend.api.post.dto.QuoteDTO;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.api.post.repository.PostRepository;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PostRepository postRepository;
    private final BookRepository bookRepository;
    private final QuoteRepository quoteRepository;

    // 리뷰 조회 (최신순) - Post 기반
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviews(String isbn, Integer page, Integer size) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page - 1, size);

        // 게시글 목록 조회 (최신순)
        Page<Post> postPage = postRepository.findByBookOrderByCreatedAtDesc(book, pageable);

        List<ReviewItemDTO> reviewItems = postPage.getContent().stream()
                .map(this::convertToReviewItemDTO)
                .collect(Collectors.toList());

        return ReviewResponseDTO.builder()
                .total(postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(reviewItems)
                .build();
    }

    private ReviewItemDTO convertToReviewItemDTO(Post post) {
        List<Quote> quotes = quoteRepository.findByPostId(post.getId());
        List<QuoteDTO> quoteDTOs = quotes.stream()
                .map(quote -> QuoteDTO.builder()
                        .quoteContent(quote.getQuoteContent())
                        .pageNumber(quote.getPageNumber())
                        .build())
                .collect(Collectors.toList());

        PostDTO.LikesInfo likesInfo = PostDTO.LikesInfo.builder()
                .relatableCount(post.getRelatableCount())
                .sameTasteCount(post.getSameTasteCount())
                .impressiveExpressionCount(post.getImpressiveExpressionCount())
                .wantToReadCount(post.getWantToReadCount())
                .helpfulCount(post.getHelpfulCount())
                .build();

        return ReviewItemDTO.builder()
                .postId(post.getId())
                .nickname(post.getMember().getNickname())
                .readingTasteType(post.getMember().getReadingTasteType())
                .profileImage(post.getMember().getProfileImage())
                .createdAt(post.getCreatedAt())
                .rating(post.getRating())
                .content(post.getContent())
                .quotes(quoteDTOs)
                .likesInfo(likesInfo)
                .build();
    }
}
