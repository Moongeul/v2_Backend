package com.moongeul.backend.api.question.repository;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.question.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // 특정 질문의 답변 작성자 목록 조회 (중복 제거)
    @Query("SELECT DISTINCT a.member FROM Answer a WHERE a.question.id = :questionId")
    List<Member> findDistinctMembersByQuestionId(@Param("questionId") Long questionId);
}
