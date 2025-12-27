package com.moongeul.backend.api.post.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.bookshelf.entity.DoneReadBookshelf;
import com.moongeul.backend.api.bookshelf.repository.DoneReadBookshelfRepository;
import com.moongeul.backend.api.bookshelf.util.BookshelfCalculator;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.post.dto.PostCreateRequestDTO;
import com.moongeul.backend.api.post.dto.PostCreateResponseDTO;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.category.repository.CategoryRepository;
import com.moongeul.backend.api.post.repository.PostRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final DoneReadBookshelfRepository doneReadBookshelfRepository;
    private final BookshelfCalculator bookshelfCalculator;

    /* 글쓰기 */
    @Transactional
    public PostCreateResponseDTO createPost(PostCreateRequestDTO postCreateRequestDTO, String email){

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Book book = bookRepository.findByIsbn(postCreateRequestDTO.getIsbn())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        Category category = categoryRepository.findById(postCreateRequestDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.CATEGORY_NOTFOUND_EXCEPTION.getMessage()));

        Post newPost = postCreateRequestDTO.toEntity(category, member, book);
        Post savedPost = postRepository.save(newPost);

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

        return PostCreateResponseDTO.builder()
                .postId(savedPost.getId())
                .build();
    }
}
