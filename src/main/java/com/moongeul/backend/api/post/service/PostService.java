package com.moongeul.backend.api.post.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.bookshelf.util.BookshelfCalculator;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.notification.service.NotificationTriggerService;
import com.moongeul.backend.api.post.dto.*;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.post.entity.*;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.api.post.repository.LikeRepository;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CategoryRepository categoryRepository;
    private final QuoteRepository quoteRepository;
    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final BookshelfCalculator bookshelfCalculator;

    private final NotificationTriggerService notificationTriggerService;
    

    /*
     * 기록
     */
    
    /* 글쓰기 */
    @Transactional
    public PostIdResponseDTO createPost(PostRequestDTO postRequestDTO, String email){

        Member member = getMemberByEmail(email);
        Book book = getBook(postRequestDTO.getIsbn());

        Category category = null;
        if(postRequestDTO.getCategoryId() != 0){
            category = getCategory(postRequestDTO.getCategoryId());

            // 예외처리: 작성하는 사람과 카테고리 주인이 같은지 확인 (본인의 카테고리인지)
            if(!category.getMember().getId().equals(member.getId())){
                throw new UnauthorizedException(ErrorStatus.CATEGORY_UNAUTHORIZED.getMessage());
            }
        }
        
        Post newPost = postRequestDTO.toEntity(category, member, book);
        Post savedPost = postRepository.save(newPost);
      
        // Quote 저장
        saveQuotes(postRequestDTO, savedPost);

        // 읽은 책 책장에 등록 또는 업데이트
        Float weight = bookshelfCalculator.calculateWeight(savedPost.getPage());
        Float height = bookshelfCalculator.calculateHeight(savedPost.getRating());

        // 기존 읽은 책이 있는지 확인
        DoneReadBookshelf doneReadBookshelf = doneReadBookshelfRepository.findByMemberAndBook(member, book)
                .orElse(null);

        if (doneReadBookshelf != null) {
            // 기존 책장이 있으면 업데이트 (가장 최근 게시글로 변경, 게시글 개수 증가)
            doneReadBookshelf.updateWithNewPost(savedPost, weight, height);
        } else {
            // 기존 책장이 없으면 새로 생성
            doneReadBookshelf = DoneReadBookshelf.builder()
                    .weight(weight)
                    .height(height)
                    .postCount(1)
                    .article(savedPost)
                    .member(member)
                    .build();
            doneReadBookshelfRepository.save(doneReadBookshelf);
        }

        return PostIdResponseDTO.builder()
                .postId(savedPost.getId())
                .build();
    }

    /* 기록(게시글) 전체 조회 */
    @Transactional
    public PostAllResponseDTO getPostAll(PostAllRequestDTO postAllRequestDTO, String email){

        Pageable pageable = PageRequest.of(postAllRequestDTO.getPage() - 1, postAllRequestDTO.getSize());

        // 빈 페이지 객체로 초기화 (null 방지)
        Page<Post> postPage = Page.empty(pageable);

        if(postAllRequestDTO.getPostVisibility().equals(PostVisibility.PUBLIC)){
            postPage = postRepository.findAll(pageable);
        } else if(postAllRequestDTO.getPostVisibility().equals(PostVisibility.FOLLOWERS) && !email.equals("anonymousUser")){
            Member member = getMemberByEmail(email);
            postPage = postRepository.findAllByFollower(member, pageable);
        }

        List<PostDTO> postDTOList = new ArrayList<>();
        if (!postPage.isEmpty()) {
            for(Post post : postPage.getContent()){
                postDTOList.add(getPostDetail(post.getId()));
            }
        }

        return PostAllResponseDTO.builder()
                .total(postPage.getTotalElements())
                .page(postPage.getNumber() + 1) // 페이지 1부터 시작(임의 지정)
                .size(postPage.getSize())
                .totalPages(postPage.getTotalPages())
                .isLast(postPage.isLast())
                .data(postDTOList)
                .build();
    }

    /* 기록(게시글) 상세 조회 */
    @Transactional
    public PostDTO getPostDetail(Long postId){

        Post post = getPost(postId);
        Book book = getBook(post.getBook().getIsbn());

        // 멤버 정보(필요 정보만) DTO
        PostDTO.MemberInfo memberInfo = PostDTO.MemberInfo.builder()
                .memberId(post.getMember().getId())
                .nickname(post.getMember().getNickname())
                .profileImage(post.getMember().getProfileImage())
                .readingTasteType(post.getMember().getReadingTasteType())
                .build();

        // 책 정보(필요 정보만) DTO
        PostDTO.BookInfo bookInfo = PostDTO.BookInfo.builder()
                .isbn(book.getIsbn())
                .bookImage(book.getBookImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .pubdate(book.getPubdate())
                .ratingAverage(book.getRatingAverage())
                .build();

        // 인상깊은구절 조회
        List<Quote> quotes = quoteRepository.findByPostId(postId); // 리스트 반환이기에 `orElseThrow()` 사용 x
        List<PostDTO.QuoteDTO> quoteDTOList = new ArrayList<>();
        for(Quote quote : quotes){
            PostDTO.QuoteDTO quoteDTO = PostDTO.QuoteDTO.builder()
                    .quoteContent(quote.getQuoteContent())
                    .pageNumber(quote.getPageNumber())
                    .build();
            quoteDTOList.add(quoteDTO);
        }

        // 공감 개수 DTO
        PostDTO.LikesInfo likesInfo = PostDTO.LikesInfo.builder()
                .relatableCount(post.getRelatableCount())
                .sameTasteCount(post.getSameTasteCount())
                .impressiveExpressionCount(post.getImpressiveExpressionCount())
                .wantToReadCount(post.getWantToReadCount())
                .helpfulCount(post.getHelpfulCount())
                .build();

        return PostDTO.builder()
                .postId(postId)
                .memberInfo(memberInfo)
                .created(post.getCreatedAt())
                .bookInfo(bookInfo)
                .rating(post.getRating())
                .content(post.getContent())
                .readDate(post.getReadDate())
                .quotesCnt(quoteDTOList.size())
                .quotes(quoteDTOList)
                .likesInfo(likesInfo)
                .build();
    }

    /* 기록(게시글) 수정 */
    @Transactional
    public PostIdResponseDTO updatePost(Long postId, String email, PostRequestDTO postRequestDTO){

        Post post = getPost(postId);
        Category category = getCategory(postRequestDTO.getCategoryId());
        Book book = getBook(post.getBook().getIsbn());

        // 예외처리: 수정하는 사람과 게시글 주인이 같은지 확인 (본인의 게시글인지)
        if (!post.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.POST_UNAUTHORIZED.getMessage());
        }

        // 예외처리: 수정하는 사람과 카테고리 주인이 같은지 확인 (본인의 카테고리인지)
        if (!category.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.CATEGORY_UNAUTHORIZED.getMessage());
        }

        // 예외처리: 수정한 책 정보가 Book 테이블에 저장되어 있지 않은 경우 -> 저장
        // (책 검색 시 저장되기 때문에 우선 처리x, 대신 책에 대한 NOT_FOUND 에러 throw)


        // (1) 기존 인상깊은구절 일괄 삭제 -> 전체 교체 방식 적용
        // 이유: 데이터가 많지 않으므로(최대 10개) 성능 이슈가 없고, 클라이언트 편의성 높이고 유지보수 간결
        quoteRepository.deleteAllByPostId(postId);

        // (2) 새로운 인상깊은구절 저장
        saveQuotes(postRequestDTO, post);

        // rating과 page가 null로 들어오면 기본값으로 대체
        Double finalRating = (postRequestDTO.getRating() != null) ? postRequestDTO.getRating() : 5.0;
        Integer finalPage = (postRequestDTO.getPage() != null) ? postRequestDTO.getPage() : 300;

        // 기록(게시글) 내용 갱신
        post.update(
                postRequestDTO.getReadDate(),
                finalRating,
                finalPage,
                postRequestDTO.getContent(),
                postRequestDTO.getPostVisibility(),
                category,
                book
        );

        return PostIdResponseDTO.builder()
                .postId(post.getId())
                .build();
    }

    /* 기록(게시글) 삭제 */
    @Transactional
    public void deletePost(Long postId, String email){

        Post post = getPost(postId);

        // 예외처리: 수정하는 사람과 게시글 주인이 같은지 확인 (본인의 게시글인지)
        if (!post.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.POST_UNAUTHORIZED.getMessage());
        }

        // 인상깊은구절 일괄 삭제
        quoteRepository.deleteAllByPostId(postId);

        // 게시글 삭제
        postRepository.delete(post);
    }
    
    
    // 새로운 인상깊은구절 저장
    private void saveQuotes(PostRequestDTO postRequestDTO, Post post){
        
        if (postRequestDTO.getQuotes() != null && !postRequestDTO.getQuotes().isEmpty()){
            for(PostRequestDTO.QuoteRequestDTO quoteRequestDTO : postRequestDTO.getQuotes()){
                Quote quote = Quote.builder()
                        .quoteContent(quoteRequestDTO.getQuoteContent())
                        .pageNumber(quoteRequestDTO.getPageNumber())
                        .post(post)
                        .build();

                quoteRepository.save(quote);
            }
        }
    }

    /*
     * 공감
     */

    /* 공감 토글 */
    @Transactional
    public void likePost(Long postId, String email, LikeDTO likeDTO){

        Member member = getMemberByEmail(email);
        Post post = getPost(postId);

        // 사용자가 해당 게시글에 누른 공감 유형이 있다면
        Optional<Likes> existingLike = likeRepository.findByPostIdAndMemberId(postId, member.getId());

        if(existingLike.isPresent()){
            Likes currentLikes = existingLike.get();

            // 사용자가 해당 게시글에 누른 "같은 공감 유형"이 있다면(또 누른 경우) -> 공감 삭제
            if (currentLikes.getLikeType().equals(likeDTO.getLikeType())) {
                decrementLikeCount(post, currentLikes.getLikeType());
                likeRepository.delete(currentLikes);
                return;
            }

            // 사용자가 해당 게시글에 누른 "다른 공감 유형"이 있다면 -> 공감 유형 수정
            decrementLikeCount(post, currentLikes.getLikeType());
            currentLikes.changeLikeType(likeDTO.getLikeType());
            incrementLikeCount(post, likeDTO.getLikeType());

            notificationTriggerService.likeNotification(post.getMember(), member, post); // 알림 발생
            return;
        }
        
        // 처음 공감을 누르는 경우 -> 새로 저장
        likeRepository.save(likeDTO.toEntity(member, post));
        incrementLikeCount(post, likeDTO.getLikeType());

        notificationTriggerService.likeNotification(post.getMember(), member, post); // 알림 발생
    }
    
    // 공감 카운트 증가 메서드
    private void incrementLikeCount(Post post, LikeType likeType) {
        switch (likeType) {
            case RELATABLE: post.incrementRelatableCount(); break;
            case SAME_TASTE: post.incrementSameTasteCount(); break;
            case IMPRESSIVE_EXPRESSION: post.incrementImpressiveExpressionCount(); break;
            case WANT_TO_READ: post.incrementWantToReadCount(); break;
            case HELPFUL: post.incrementHelpfulCount(); break;
        }
    }

    // 공감 카운트 감소 메서드
    private void decrementLikeCount(Post post, LikeType likeType) {
        switch (likeType) {
            case RELATABLE: post.decrementRelatableCount(); break;
            case SAME_TASTE: post.decrementSameTasteCount(); break;
            case IMPRESSIVE_EXPRESSION: post.decrementImpressiveExpressionCount(); break;
            case WANT_TO_READ: post.decrementWantToReadCount(); break;
            case HELPFUL: post.decrementHelpfulCount(); break;
        }
    }

    /*
     * 추천
     */
    
    // 주간 추천 기록 조회
    @Transactional(readOnly = true)
    public WeeklyRecommendationResponseDTO getWeeklyRecommendation(String email) {
        Member member = getMemberByEmail(email);
        
        // 사용자의 독서 취향이 없으면 예외 처리
        if (member.getReadingTasteType() == null) {
            throw new NotFoundException(ErrorStatus.USER_READING_TASTE_NOT_FOUND_EXCEPTION.getMessage());
        }

        // 이번 주 월요일 00:00:00부터 오늘까지 계산
        LocalDate today = LocalDate.now();
        LocalDate thisWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = thisWeekMonday.atStartOfDay();

        // 같은 취향 사용자들의 기록 중 이번 주(월요일~오늘) 가장 공감을 많이 받은 기록 조회
        // 전체 기록을 조회한 후 정렬하여 가장 공감을 많이 받은 기록 선택
        List<Post> recommendedPosts = postRepository.findWeeklyRecommendationByReadingTasteType(
                member.getReadingTasteType(),
                weekStart
        );

        // 추천 기록이 없으면 예외 처리
        if (recommendedPosts.isEmpty()) {
            throw new NotFoundException(ErrorStatus.WEEKLY_RECOMMENDATION_NOT_FOUND_EXCEPTION.getMessage());
        }

        // 가장 공감을 많이 받은 기록 (첫 번째 요소 - ORDER BY로 정렬된 결과)
        Post post = recommendedPosts.get(0);

        return WeeklyRecommendationResponseDTO.builder()
                .postId(post.getId())
                .bookImage(post.getBook().getBookImage())
                .bookTitle(post.getBook().getTitle())
                .isbn(post.getBook().getIsbn())
                .author(post.getBook().getAuthor())
                .publisher(post.getBook().getPublisher())
                .pubdate(post.getBook().getPubdate())
                .bookRating(post.getBook().getRatingAverage())
                .rating(post.getRating())
                .content(post.getContent())
                .readingTasteType(member.getReadingTasteType())
                .build();
    }

    /*
     * 단순 데이터 불러오기용 코드 메서드 - 코드 깔끔하게 하기용
     */

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Book getBook(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.POST_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));
    }
}
