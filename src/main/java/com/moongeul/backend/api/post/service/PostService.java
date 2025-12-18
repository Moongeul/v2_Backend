package com.moongeul.backend.api.post.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
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

        return PostCreateResponseDTO.builder()
                .postId(savedPost.getId())
                .build();
    }
}
