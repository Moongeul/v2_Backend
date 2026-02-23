package com.moongeul.backend.api.readingTaste.repository;

import com.moongeul.backend.api.readingTaste.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestResultRepository  extends JpaRepository<TestResult, Long> {

    // guestUuid로 찾아 생성일(CreatedAt) 내림차순(Desc) 정렬 후 첫 번째(First) 행 반환
    Optional<TestResult> findFirstByGuestUuidOrderByCreatedAtDesc(String guestUuid);
}
