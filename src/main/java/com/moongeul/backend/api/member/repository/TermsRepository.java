package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Terms;
import com.moongeul.backend.api.member.entity.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findByTermsType(TermsType type);
}
