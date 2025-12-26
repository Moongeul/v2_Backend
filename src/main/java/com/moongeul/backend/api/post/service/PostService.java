package com.moongeul.backend.api.post.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.post.dto.PostRequestDTO;
import com.moongeul.backend.api.post.dto.PostIdResponseDTO;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.post.dto.PostResponseDTO;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.api.post.entity.Quote;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.api.post.repository.QuoteRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final QuoteRepository quoteRepository;

    /* 글쓰기 */
    @Transactional
    public PostIdResponseDTO createPost(PostRequestDTO postRequestDTO, String email){

        Member member = getMemberByEmail(email);
        Book book = getBook(postRequestDTO.getIsbn());

        Category category = null;
        if(postRequestDTO.getCategoryId() != 0){
            category = getCategory(postRequestDTO.getCategoryId());
        }
        
        Post newPost = postRequestDTO.toEntity(category, member, book);
        Post savedPost = postRepository.save(newPost);

        // Quote 저장
        saveQuotes(postRequestDTO, savedPost);

        return PostIdResponseDTO.builder()
                .postId(savedPost.getId())
                .build();
    }

    /* 기록(게시글) 상세 조회 */
    @Transactional
    public PostResponseDTO getPostDetail(Long postId){

        Post post = getPost(postId);
        Book book = getBook(post.getBook().getIsbn());

        // 책 정보(필요 정보만) DTO
        PostResponseDTO.BookInfo bookInfo = PostResponseDTO.BookInfo.builder()
                .isbn(book.getIsbn())
                .bookImage(book.getBookImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .build();

        // 인상깊은구절 조회
        List<Quote> quotes = quoteRepository.findByPostId(postId); // 리스트 반환이기에 `orElseThrow()` 사용 x
        List<PostResponseDTO.QuoteDTO> quoteDTOList = new ArrayList<>();
        for(Quote quote : quotes){
            PostResponseDTO.QuoteDTO quoteDTO = PostResponseDTO.QuoteDTO.builder()
                    .quoteContent(quote.getQuoteContent())
                    .pageNumber(quote.getPageNumber())
                    .build();
            quoteDTOList.add(quoteDTO);
        }

        return PostResponseDTO.builder()
                .bookInfo(bookInfo)
                .rating(post.getRating())
                .content(post.getContent())
                .quotes(quoteDTOList)
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
