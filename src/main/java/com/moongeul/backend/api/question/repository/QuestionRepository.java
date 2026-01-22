package com.moongeul.backend.api.question.repository;

import com.moongeul.backend.api.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionRepository extends JpaRepository<Question, Long>  {

    // 질문 전체 조회 (최신순)
    @Query("SELECT q FROM Question q ORDER BY q.createdAt DESC")
    Page<Question> findAllQuestions(Pageable pageable);
}
