package com.moongeul.backend.api.question.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.question.dto.QuestionCreateRequestDTO;
import com.moongeul.backend.api.question.dto.QuestionIdResponseDTO;
import com.moongeul.backend.api.question.entity.Question;
import com.moongeul.backend.api.question.repository.QuestionRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;

    /* 질문 생성 */
    public QuestionIdResponseDTO createQuestion(QuestionCreateRequestDTO questionCreateRequestDTO, String email){

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        Book book = bookRepository.findByIsbn(questionCreateRequestDTO.getIsbn())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        Question newQuestion = questionCreateRequestDTO.toEntity(member, book);
        Question savedQuestion = questionRepository.save(newQuestion);

        return QuestionIdResponseDTO.builder()
                .questionId(savedQuestion.getId())
                .build();
    }
}
