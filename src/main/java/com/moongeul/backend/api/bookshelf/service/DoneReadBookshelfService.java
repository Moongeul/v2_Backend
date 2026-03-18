package com.moongeul.backend.api.bookshelf.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookPostListResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarDayDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfItemDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadRatingRangeCountDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadRatingSummaryResponseDTO;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
import com.moongeul.backend.api.post.dto.PostDTO;
import com.moongeul.backend.api.post.entity.LikeType;
import com.moongeul.backend.api.post.entity.Likes;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.repository.LikeRepository;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.ForbiddenException;
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
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoneReadBookshelfService {

    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final QuoteRepository quoteRepository;
    private final LikeRepository likeRepository;
    private final BookRepository bookRepository;
    private static final String[] RATING_RANGES = {
            "1.0~1.4", "1.5~1.9", "2.0~2.4", "2.5~2.9",
            "3.0~3.4", "3.5~3.9", "4.0~4.4", "4.5~5.0"
    };
    private static final double[] RANGE_STARTS = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5};
    private static final double[] RANGE_ENDS = {1.4, 1.9, 2.4, 2.9, 3.4, 3.9, 4.4, 5.0};

    @Transactional(readOnly = true)
    public DoneReadBookshelfResponseDTO getDoneReadBooks(String email, Long userId, Integer page, Integer size) {
        Member member = getTargetMember(email, userId);

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
    public DoneReadCalendarResponseDTO getDoneReadCalendar(String email, Long userId, Integer year, Integer month) {
        Member member = getTargetMember(email, userId);

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
    public DoneReadRatingSummaryResponseDTO getDoneReadRatingSummary(String email, Long userId) {
        Member member = getTargetMember(email, userId);

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

    // 읽은 책별 기록 리스트 조회
    @Transactional(readOnly = true)
    public DoneReadBookPostListResponseDTO getDoneReadBookPosts(String email, Long userId, String isbn, Integer page, Integer size) {
        Member currentMember = getCurrentMemberOrNull(email);
        Member targetMember = getTargetMember(currentMember, userId);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Post> postPage = postRepository.findByMemberAndBookIsbnOrderByCreatedAtDesc(targetMember, isbn, pageable);

        List<PostDTO> postList = postPage.getContent().stream()
                .map(post -> convertToPostDTO(post, currentMember))
                .collect(Collectors.toList());

        return DoneReadBookPostListResponseDTO.builder()
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .total(postPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(postList)
                .build();
    }

    // 읽은 책 별점 구간 상세 조회
    @Transactional(readOnly = true)
    public CategoryPostListResponseDTO getDoneReadRatingDetail(String email, Long userId, String range, String sortBy, Integer page, Integer size) {
        Member currentMember = getCurrentMemberOrNull(email);
        Member targetMember = getTargetMember(currentMember, userId);

        int rangeIndex = findRangeIndex(range);
        Pageable pageable = PageRequest.of(page - 1, size, resolveSort(sortBy));

        Page<Post> postPage = postRepository.findByMemberAndRatingBetween(
                targetMember,
                RANGE_STARTS[rangeIndex],
                RANGE_ENDS[rangeIndex],
                pageable
        );

        List<PostDTO> postList = postPage.getContent().stream()
                .map(post -> convertToPostDTO(post, currentMember))
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
                .postId(doneReadBookshelf.getArticle().getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .ratingAverage(book.getRatingAverage())
                .ratingCount(book.getRatingCount())
                .weight(doneReadBookshelf.getWeight())
                .height(doneReadBookshelf.getHeight())
                .postCount(doneReadBookshelf.getPostCount())
                .build();
    }

    private PostDTO convertToPostDTO(Post post, Member currentMember) {

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

        PostDTO.MyLikesStatus myLikesStatus = convertToMyLikesStatus(currentMember, post.getId());

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
                .myLikesStatus(myLikesStatus)
                .build();
    }

    private PostDTO.MyLikesStatus convertToMyLikesStatus(Member currentMember, Long postId) {
        if (currentMember == null) {
            return PostDTO.MyLikesStatus.empty();
        }

        List<Likes> myLikes = likeRepository.findByPostIdAndMemberId(postId, currentMember.getId());

        if (myLikes.isEmpty()) {
            return PostDTO.MyLikesStatus.empty();
        }

        Set<LikeType> myLikesTypes = myLikes.stream()
                .map(Likes::getLikeType)
                .collect(Collectors.toSet());

        return PostDTO.MyLikesStatus.builder()
                .relatableCount(myLikesTypes.contains(LikeType.RELATABLE))
                .sameTasteCount(myLikesTypes.contains(LikeType.SAME_TASTE))
                .impressiveExpressionCount(myLikesTypes.contains(LikeType.IMPRESSIVE_EXPRESSION))
                .wantToReadCount(myLikesTypes.contains(LikeType.WANT_TO_READ))
                .helpfulCount(myLikesTypes.contains(LikeType.HELPFUL))
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

    private Member getTargetMember(String email, Long userId) {
        Member currentMember = getCurrentMemberOrNull(email);
        return getTargetMember(currentMember, userId);
    }

    private Member getCurrentMemberOrNull(String email) {
        if (email == null || "anonymousUser".equals(email)) {
            return null;
        }

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Member getTargetMember(Member currentMember, Long userId) {
        Member targetMember;
        if (userId != null) {
            targetMember = memberRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
        } else if (currentMember != null) {
            targetMember = currentMember;
        } else {
            throw new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage());
        }

        validatePrivacyAccess(currentMember, targetMember);
        return targetMember;
    }

    private void validatePrivacyAccess(Member currentMember, Member targetMember) {
        if (currentMember != null && currentMember.getId().equals(targetMember.getId())) {
            return;
        }

        PrivacyLevel privacyLevel = targetMember.getPrivacyLevel();

        if (privacyLevel == null || privacyLevel == PrivacyLevel.PUBLIC) {
            return;
        }

        if (privacyLevel == PrivacyLevel.PRIVATE) {
            throw new ForbiddenException(ErrorStatus.PRIVACY_FORBIDDEN_EXCEPTION.getMessage());
        }

        if (privacyLevel == PrivacyLevel.FOLLOWER_ONLY) {
            if (currentMember == null) {
                throw new ForbiddenException(ErrorStatus.PRIVACY_FORBIDDEN_EXCEPTION.getMessage());
            }

            FollowStatus status = followRepository.findByFollowingIdAndFollowerId(targetMember.getId(), currentMember.getId())
                    .map(Follow::getFollowStatus)
                    .orElse(FollowStatus.NONE);

            if (status != FollowStatus.ACCEPTED) {
                throw new ForbiddenException(ErrorStatus.PRIVACY_FORBIDDEN_EXCEPTION.getMessage());
            }
        }
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
