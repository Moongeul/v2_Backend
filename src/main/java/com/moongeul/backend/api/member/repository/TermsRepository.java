package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
