package com.moongeul.backend.api.question.service;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.book.repository.BookRepository;
import com.moongeul.backend.api.member.entity.Follow;
import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.member.repository.FollowRepository;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.question.dto.QuestionCreateRequestDTO;
import com.moongeul.backend.api.question.dto.QuestionDTO;
import com.moongeul.backend.api.question.dto.QuestionIdResponseDTO;
import com.moongeul.backend.api.question.dto.QuestionListResponseDTO;
import com.moongeul.backend.api.question.dto.QuestionModifyRequestDTO;
import com.moongeul.backend.api.question.entity.Question;
import com.moongeul.backend.api.question.repository.AnswerRepository;
import com.moongeul.backend.api.question.repository.QuestionRepository;
import com.moongeul.backend.common.exception.ForbiddenException;
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final BookRepository bookRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    /* 질문 생성 */
    @Transactional
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

    // 질문 리스트 조회
    public QuestionListResponseDTO getQuestionList(Integer page, Integer size, String email) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Question> questionPage = questionRepository.findAllQuestions(pageable);

        List<QuestionDTO> questionDTOList = questionPage.getContent().stream()
                .map(question -> convertToQuestionDTO(question, email))
                .collect(Collectors.toList());

        return QuestionListResponseDTO.builder()
                .total(questionPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(questionPage.getTotalPages())
                .isLast(questionPage.isLast())
                .data(questionDTOList)
                .build();
    }

    // 마이페이지 질문 리스트 조회
    public QuestionListResponseDTO getMyQuestionList(Integer page, Integer size, String email, Long userId) {

        Member currentMember = getCurrentMemberOrNull(email);
        Member targetMember = getTargetMember(currentMember, userId);

        validatePrivacyAccess(currentMember, targetMember);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Question> questionPage = questionRepository.findByMemberIdOrderByCreatedAtDesc(targetMember.getId(), pageable);

        List<QuestionDTO> questionDTOList = questionPage.getContent().stream()
                .map(question -> convertToQuestionDTO(question, email))
                .collect(Collectors.toList());

        return QuestionListResponseDTO.builder()
                .total(questionPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(questionPage.getTotalPages())
                .isLast(questionPage.isLast())
                .data(questionDTOList)
                .build();
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

    // 질문 상세 조회
    public QuestionDTO getQuestionDetail(Long questionId, String email) {

        // 질문 조회
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.QUESTION_NOTFOUND_EXCEPTION.getMessage()));

        return convertToQuestionDTO(question, email);
    }

    // 질문 수정
    @Transactional
    public QuestionIdResponseDTO modifyQuestion(Long questionId, QuestionModifyRequestDTO requestDTO, String email) {

        // 질문 조회
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.QUESTION_NOTFOUND_EXCEPTION.getMessage()));

        // 작성자 확인 (자신이 작성한 질문만 수정 가능)
        if (!question.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.QUESTION_UNAUTHORIZED.getMessage());
        }

        // 책 조회
        Book book = bookRepository.findByIsbn(requestDTO.getIsbn())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.BOOK_NOTFOUND_EXCEPTION.getMessage()));

        // 질문 수정 (내용과 책)
        question.modify(requestDTO.getContent(), book);

        log.info("질문 수정 완료 - 질문 ID: {}, 작성자: {}, 새 ISBN: {}", questionId, email, requestDTO.getIsbn());

        return QuestionIdResponseDTO.builder()
                .questionId(question.getId())
                .build();
    }

    // 질문 삭제
    @Transactional
    public void deleteQuestion(Long questionId, String email) {

        // 질문 조회
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.QUESTION_NOTFOUND_EXCEPTION.getMessage()));

        // 작성자 확인 (자신이 작성한 질문만 삭제 가능)
        if (!question.getMember().getEmail().equals(email)) {
            throw new UnauthorizedException(ErrorStatus.QUESTION_UNAUTHORIZED.getMessage());
        }

        // 연관된 모든 답변 삭제 (하드 삭제)
        answerRepository.deleteByQuestionId(questionId);

        questionRepository.delete(question);

        log.info("질문 삭제 완료 - 질문 ID: {}, 작성자: {}", questionId, email);
    }

    // Question 엔티티를 QuestionDTO로 변환
    private QuestionDTO convertToQuestionDTO(Question question, String email) {

        Book book = question.getBook();

        // 답변 작성자 목록 조회 (중복 제거)
        List<Member> answerMembers = answerRepository.findDistinctMembersByQuestionId(question.getId());

        // 참여자 목록 = 질문 작성자 + 답변 작성자 (중복 제거)
        Set<Member> participantSet = new LinkedHashSet<>();
        participantSet.add(question.getMember()); // 질문 작성자 추가
        participantSet.addAll(answerMembers); // 답변 작성자들 추가

        // 참여자 프로필 이미지 (최대 3명)
        List<String> participantProfileImages = participantSet.stream()
                .limit(3)
                .map(Member::getProfileImage)
                .collect(Collectors.toList());

        // 내가 작성한 질문인지 확인
        boolean isMyArticle = question.getMember().getEmail().equals(email);

        return QuestionDTO.builder()
                .questionId(question.getId())
                .content(question.getContent())
                .commentCnt(question.getCommentCnt())
                .createdAt(question.getCreatedAt())
                .myArticle(isMyArticle)
                .bookInfo(QuestionDTO.BookInfo.builder()
                        .isbn(book.getIsbn())
                        .bookImage(book.getBookImage())
                        .title(book.getTitle())
                        .author(book.getAuthor())
                        .publisher(book.getPublisher())
                        .pubdate(book.getPubdate())
                        .ratingAverage(book.getRatingAverage())
                        .build())
                .participantCount(participantSet.size())
                .participantProfileImages(participantProfileImages)
                .build();
    }

    private Member getCurrentMemberOrNull(String email) {
        if (email == null || "anonymousUser".equals(email)) {
            return null;
        }

        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }

    private Member getTargetMember(Member currentMember, Long userId) {
        if (userId != null) {
            return memberRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
        }

        if (currentMember != null) {
            return currentMember;
        }

        throw new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage());
    }
}
