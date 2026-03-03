package com.moongeul.backend.api.question.repository;

import com.moongeul.backend.api.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long>  {

    // 질문 전체 조회 (최신순)
    @Query("SELECT q FROM Question q ORDER BY q.createdAt DESC")
    Page<Question> findAllQuestions(Pageable pageable);

    // 특정 사용자가 작성한 질문 조회 (최신순)
    Page<Question> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    // 회원 탈퇴 시, 해당 회원이 작성한 모든 질문 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Question q WHERE q.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
