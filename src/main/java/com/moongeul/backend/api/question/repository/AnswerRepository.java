package com.moongeul.backend.api.question.repository;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.question.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 특정 질문의 답변 작성자 목록 조회 (중복 제거)
    @Query("SELECT DISTINCT a.member FROM Answer a WHERE a.question.id = :questionId")
    List<Member> findDistinctMembersByQuestionId(@Param("questionId") Long questionId);

    // 특정 질문의 모든 답변 삭제
    void deleteByQuestionId(Long questionId);

    // 특정 질문의 답변 리스트 조회 (페이징, 최신순)
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId ORDER BY a.createdAt DESC")
    Page<Answer> findByQuestionId(@Param("questionId") Long questionId, Pageable pageable);

    // 회원 탈퇴 시, 해당 회원이 작성한 모든 답변 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Answer a WHERE a.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    // 회원 탈퇴 시 해당 회원이 작성한 '질문'에 달린 모든 답변 삭제
    // 부모인 질문 삭제 전에 먼저 정리해야 함
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Answer a WHERE a.question.id IN (SELECT q.id FROM Question q WHERE q.member.id = :memberId)")
    void deleteAllByQuestionMemberId(@Param("memberId") Long memberId);
}
