package com.moongeul.backend.api.bookshelf.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarDayDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfItemDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadRatingRangeCountDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadRatingSummaryResponseDTO;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
import com.moongeul.backend.api.post.dto.PostDTO;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoneReadBookshelfService {

    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final QuoteRepository quoteRepository;
    private static final String[] RATING_RANGES = {
            "1.0~1.4", "1.5~1.9", "2.0~2.4", "2.5~2.9",
            "3.0~3.4", "3.5~3.9", "4.0~4.4", "4.5~5.0"
    };
    private static final double[] RANGE_STARTS = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5};
    private static final double[] RANGE_ENDS = {1.4, 1.9, 2.4, 2.9, 3.4, 3.9, 4.4, 5.0};

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
                .data(books)
                .build();
    }

    // 읽은 책 캘린더 조회
    @Transactional(readOnly = true)
    public DoneReadCalendarResponseDTO getDoneReadCalendar(String email, Integer year, Integer month) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Post> posts = postRepository.findCalendarPostsByMemberAndReadDateBetweenOrderByReadDateAscCreatedAtDesc(
                member, startDate, endDate);

        Map<LocalDate, CalendarDayAggregate> calendarMap = new LinkedHashMap<>();

        for (Post post : posts) {
            LocalDate readDate = post.getReadDate();
            if (readDate == null) {
                continue;
            }

            CalendarDayAggregate aggregate = calendarMap.get(readDate);
            if (aggregate == null) {
                calendarMap.put(readDate, new CalendarDayAggregate(
                        post.getId(),
                        post.getBook().getIsbn(),
                        post.getBook().getBookImage(),
                        post.getCreatedAt(),
                        1
                ));
                continue;
            }

            aggregate.count++;
            boolean isMoreRecent = aggregate.latestCreatedAt == null
                    || (post.getCreatedAt() != null && post.getCreatedAt().isAfter(aggregate.latestCreatedAt));
            boolean isSameTimeButLargerId = post.getCreatedAt() != null
                    && aggregate.latestCreatedAt != null
                    && post.getCreatedAt().isEqual(aggregate.latestCreatedAt)
                    && aggregate.postId != null
                    && post.getId() != null
                    && post.getId() > aggregate.postId;

            if (isMoreRecent || isSameTimeButLargerId) {
                aggregate.postId = post.getId();
                aggregate.isbn = post.getBook().getIsbn();
                aggregate.bookImage = post.getBook().getBookImage();
                aggregate.latestCreatedAt = post.getCreatedAt();
            }
        }

        List<DoneReadCalendarDayDTO> calendarData = calendarMap.entrySet().stream()
                .map(entry -> DoneReadCalendarDayDTO.builder()
                        .day(entry.getKey().getDayOfMonth())
                        .postId(entry.getValue().postId)
                        .isbn(entry.getValue().isbn)
                        .bookImage(entry.getValue().bookImage)
                        .count(entry.getValue().count)
                        .build())
                .toList();

        return DoneReadCalendarResponseDTO.builder()
                .year(year)
                .month(month)
                .data(calendarData)
                .build();
    }

    // 읽은 책 별점 요약 조회
    @Transactional(readOnly = true)
    public DoneReadRatingSummaryResponseDTO getDoneReadRatingSummary(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        long totalBooks = doneReadBookshelfRepository.countByMember(member);
        List<Double> ratings = postRepository.findRatingsByMember(member);

        int[] counts = new int[RATING_RANGES.length];
        for (Double rating : ratings) {
            if (rating == null) {
                continue;
            }

            for (int i = 0; i < RATING_RANGES.length; i++) {
                if (rating >= RANGE_STARTS[i] && rating <= RANGE_ENDS[i] + 1e-9) {
                    counts[i]++;
                    break;
                }
            }
        }

        List<DoneReadRatingRangeCountDTO> data = new ArrayList<>();
        for (int i = 0; i < RATING_RANGES.length; i++) {
            data.add(DoneReadRatingRangeCountDTO.builder()
                    .range(RATING_RANGES[i])
                    .count(counts[i])
                    .build());
        }

        return DoneReadRatingSummaryResponseDTO.builder()
                .totalBooks(totalBooks)
                .data(data)
                .build();
    }

    // 읽은 책 별점 구간 상세 조회
    @Transactional(readOnly = true)
    public CategoryPostListResponseDTO getDoneReadRatingDetail(String email, String range, String sortBy, Integer page, Integer size) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        int rangeIndex = findRangeIndex(range);
        Pageable pageable = PageRequest.of(page - 1, size, resolveSort(sortBy));

        Page<Post> postPage = postRepository.findByMemberAndRatingBetween(
                member,
                RANGE_STARTS[rangeIndex],
                RANGE_ENDS[rangeIndex],
                pageable
        );

        List<PostDTO> postList = postPage.getContent().stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());

        return CategoryPostListResponseDTO.builder()
                .total(postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(postList)
                .build();
    }

    private DoneReadBookshelfItemDTO convertToItemDTO(DoneReadBookshelf doneReadBookshelf) {
        Book book = doneReadBookshelf.getArticle().getBook();
        
        return DoneReadBookshelfItemDTO.builder()
                .articleId(doneReadBookshelf.getArticle().getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .ratingAverage(book.getRatingAverage())
                .ratingCount(book.getRatingCount())
                .weight(doneReadBookshelf.getWeight())
                .height(doneReadBookshelf.getHeight())
                .postCount(doneReadBookshelf.getPostCount())
                .build();
    }

    private PostDTO convertToPostDTO(Post post) {

        Book book = post.getBook();

        PostDTO.MemberInfo memberInfo = PostDTO.MemberInfo.builder()
                .memberId(post.getMember().getId())
                .nickname(post.getMember().getNickname())
                .profileImage(post.getMember().getProfileImage())
                .readingTasteType(post.getMember().getReadingTasteType())
                .build();

        PostDTO.BookInfo bookInfo = PostDTO.BookInfo.builder()
                .isbn(book.getIsbn())
                .bookImage(book.getBookImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .pubdate(book.getPubdate())
                .ratingAverage(book.getRatingAverage())
                .build();

        List<Quote> quotes = quoteRepository.findByPostId(post.getId());
        List<PostDTO.QuoteDTO> quoteDTOList = quotes.stream()
                .map(quote -> PostDTO.QuoteDTO.builder()
                        .quoteContent(quote.getQuoteContent())
                        .pageNumber(quote.getPageNumber())
                        .build())
                .collect(Collectors.toList());

        PostDTO.LikesCnt likesCnt = PostDTO.LikesCnt.builder()
                .relatableCount(post.getRelatableCount())
                .sameTasteCount(post.getSameTasteCount())
                .impressiveExpressionCount(post.getImpressiveExpressionCount())
                .wantToReadCount(post.getWantToReadCount())
                .helpfulCount(post.getHelpfulCount())
                .build();

        return PostDTO.builder()
                .postId(post.getId())
                .memberInfo(memberInfo)
                .created(post.getCreatedAt())
                .bookInfo(bookInfo)
                .rating(post.getRating())
                .content(post.getContent())
                .readDate(post.getReadDate())
                .quotesCnt(quoteDTOList.size())
                .quotes(quoteDTOList)
                .likesCnt(likesCnt)
                .build();
    }

    private Sort resolveSort(String sortBy) {
        if (sortBy == null) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        return switch (sortBy.toUpperCase()) {
            case "OLDEST" -> Sort.by(Sort.Order.asc("createdAt"));
            case "RATING_HIGH" -> Sort.by(Sort.Order.desc("rating"), Sort.Order.desc("createdAt"));
            case "RATING_LOW" -> Sort.by(Sort.Order.asc("rating"), Sort.Order.desc("createdAt"));
            case "LATEST" -> Sort.by(Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }

    private int findRangeIndex(String range) {
        if (range == null) {
            throw new BadRequestException(ErrorStatus.INVALID_RATING_RANGE_EXCEPTION.getMessage());
        }

        String normalizedRange = range.replace(" ", "");
        for (int i = 0; i < RATING_RANGES.length; i++) {
            if (RATING_RANGES[i].equals(normalizedRange)) {
                return i;
            }
        }

        throw new BadRequestException(ErrorStatus.INVALID_RATING_RANGE_EXCEPTION.getMessage());
    }

    private static class CalendarDayAggregate {
        private Long postId;
        private String isbn;
        private String bookImage;
        private LocalDateTime latestCreatedAt;
        private Integer count;

        private CalendarDayAggregate(Long postId, String isbn, String bookImage, LocalDateTime latestCreatedAt, Integer count) {
            this.postId = postId;
            this.isbn = isbn;
            this.bookImage = bookImage;
            this.latestCreatedAt = latestCreatedAt;
            this.count = count;
        }
    }
}
