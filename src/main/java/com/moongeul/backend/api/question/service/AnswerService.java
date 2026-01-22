package com.moongeul.backend.api.question.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.question.dto.AnswerCreateRequestDTO;
import com.moongeul.backend.api.question.dto.AnswerDTO;
import com.moongeul.backend.api.question.dto.AnswerIdResponseDTO;
import com.moongeul.backend.api.question.dto.AnswerListResponseDTO;
import com.moongeul.backend.api.question.dto.AnswerModifyRequestDTO;
import com.moongeul.backend.api.question.entity.Answer;
import com.moongeul.backend.api.question.entity.Question;
import com.moongeul.backend.api.question.repository.AnswerRepository;
import com.moongeul.backend.api.question.repository.QuestionRepository;
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

        // 질문의 댓글 수 증가
        question.increaseCommentCnt();

        log.info("답변 생성 완료 - 답변 ID: {}, 질문 ID: {}, 작성자: {}, 현재 댓글 수: {}",
                savedAnswer.getId(), question.getId(), member.getEmail(), question.getCommentCnt());

        return AnswerIdResponseDTO.builder()
                .answerId(savedAnswer.getId())
                .build();
    }

    // 답변 리스트 조회
    public AnswerListResponseDTO getAnswerList(Long questionId, Integer page, Integer size, String email) {

        // 질문 존재 여부 확인
        questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.QUESTION_NOTFOUND_EXCEPTION.getMessage()));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Answer> answerPage = answerRepository.findByQuestionId(questionId, pageable);

        List<AnswerDTO> answerDTOList = answerPage.getContent().stream()
                .map(answer -> convertToAnswerDTO(answer, email))
                .collect(Collectors.toList());

        return AnswerListResponseDTO.builder()
                .total(answerPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(answerPage.getTotalPages())
                .isLast(answerPage.isLast())
                .data(answerDTOList)
                .build();
    }

    // 답변 수정
    @Transactional
    public AnswerIdResponseDTO modifyAnswer(Long answerId, AnswerModifyRequestDTO requestDTO, String email) {

        // 답변 조회
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.ANSWER_NOTFOUND_EXCEPTION.getMessage()));

        // 작성자 확인 (자신이 작성한 답변만 수정 가능)
        if (!answer.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.ANSWER_UNAUTHORIZED.getMessage());
        }

        // 답변 내용 수정
        answer.modify(requestDTO.getContent());

        log.info("답변 수정 완료 - 답변 ID: {}, 작성자: {}", answerId, email);

        return AnswerIdResponseDTO.builder()
                .answerId(answer.getId())
                .build();
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(Long answerId, String email) {

        // 답변 조회
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.ANSWER_NOTFOUND_EXCEPTION.getMessage()));

        // 작성자 확인 (자신이 작성한 답변만 삭제 가능)
        if (!answer.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.ANSWER_UNAUTHORIZED.getMessage());
        }

        // 질문의 댓글 수 감소
        Question question = answer.getQuestion();
        question.decreaseCommentCnt();

        // 답변 삭제 (하드 삭제)
        answerRepository.delete(answer);

        log.info("답변 삭제 완료 - 답변 ID: {}, 작성자: {}, 질문 ID: {}, 현재 댓글 수: {}",
                answerId, email, question.getId(), question.getCommentCnt());
    }

    // Answer 엔티티를 AnswerDTO로 변환
    private AnswerDTO convertToAnswerDTO(Answer answer, String email) {

        Member member = answer.getMember();

        // 내가 작성한 답변인지 확인
        boolean isMyAnswer = member.getEmail().equals(email);

        return AnswerDTO.builder()
                .answerId(answer.getId())
                .content(answer.getContent())
                .createdAt(answer.getCreatedAt())
                .myAnswer(isMyAnswer)
                .memberInfo(AnswerDTO.MemberInfo.builder()
                        .profileImage(member.getProfileImage())
                        .nickname(member.getNickname())
                        .readingTasteType(member.getReadingTasteType())
                        .build())
                .build();
    }
}
