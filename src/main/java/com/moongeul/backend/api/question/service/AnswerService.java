package com.moongeul.backend.api.question.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.question.dto.AnswerCreateRequestDTO;
import com.moongeul.backend.api.question.dto.AnswerIdResponseDTO;
import com.moongeul.backend.api.question.entity.Answer;
import com.moongeul.backend.api.question.entity.Question;
import com.moongeul.backend.api.question.repository.AnswerRepository;
import com.moongeul.backend.api.question.repository.QuestionRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final MemberRepository memberRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    // 답변 생성
    @Transactional
    public AnswerIdResponseDTO createAnswer(AnswerCreateRequestDTO answerCreateRequestDTO, String email) {

        // 사용자 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 질문 조회
        Question question = questionRepository.findById(answerCreateRequestDTO.getQuestionId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.QUESTION_NOTFOUND_EXCEPTION.getMessage()));

        // 답변 생성
        Answer newAnswer = answerCreateRequestDTO.toEntity(member, question);
        Answer savedAnswer = answerRepository.save(newAnswer);

        log.info("답변 생성 완료 - 답변 ID: {}, 질문 ID: {}, 작성자: {}",
                savedAnswer.getId(), question.getId(), member.getEmail());

        return AnswerIdResponseDTO.builder()
                .answerId(savedAnswer.getId())
                .build();
    }
}
