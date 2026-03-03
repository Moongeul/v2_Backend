package com.moongeul.backend.api.setting.repository;

import com.moongeul.backend.api.setting.entity.Agree;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.setting.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AgreeRepository extends JpaRepository<Agree, Long> {

    Optional<Agree> findByMemberAndTerms(Member member, Terms terms);

    // 회원 탈퇴 시 해당 회원의 모든 약관 동의 기록 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Agree a WHERE a.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}
