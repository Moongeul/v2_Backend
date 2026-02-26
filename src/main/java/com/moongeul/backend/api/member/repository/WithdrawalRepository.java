package com.moongeul.backend.api.member.repository;

import com.moongeul.backend.api.member.entity.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
}
