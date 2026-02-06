package com.moongeul.backend.api.setting.repository;

import com.moongeul.backend.api.setting.entity.InfoOpen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InfoOpenRepository extends JpaRepository<InfoOpen, Long> {

    Optional<InfoOpen> findByMemberId(Long memberId);
}
