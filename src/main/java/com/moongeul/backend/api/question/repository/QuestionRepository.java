package com.moongeul.backend.api.question.repository;

import com.moongeul.backend.api.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long>  {
}
