package com.moongeul.backend.api.setting.repository;

import com.moongeul.backend.api.setting.entity.Agree;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.setting.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgreeRepository extends JpaRepository<Agree, Long> {

    Optional<Agree> findByMemberAndTerms(Member member, Terms terms);
}
