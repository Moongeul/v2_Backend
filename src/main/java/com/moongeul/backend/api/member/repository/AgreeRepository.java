package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Agree;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgreeRepository extends JpaRepository<Agree, Long> {
}
