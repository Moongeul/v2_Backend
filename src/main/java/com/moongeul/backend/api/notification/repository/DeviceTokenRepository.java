package com.moongeul.backend.api.notification.repository;

import com.moongeul.backend.api.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    // 특정 회원의 모든 기기 토큰 조회 (알림 보낼 때 필요)
    List<DeviceToken> findAllByMemberId(Long memberId);

    // 이미 존재하는 토큰인지 확인
    Optional<DeviceToken> findByToken(String token);

    // 로그아웃 시 특정 토큰 삭제
    void deleteByToken(String token);

    // 회원 탈퇴 시 해당 회원의 모든 디바이스 토큰 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM DeviceToken dt WHERE dt.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}