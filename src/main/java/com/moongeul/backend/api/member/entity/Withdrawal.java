package com.moongeul.backend.api.member.entity;

import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder // 빌더 패턴 사용을 위한 롬복 애너테이션
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Table(name = "WITHDRAWAL") // 데이터베이스 테이블 이름 지정
public class Withdrawal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email; // 재가입 방지 확인용

    @Column(nullable = false)
    private String reason; // 탈퇴 사유 (선택한 항목 또는 기타 입력값)
    private String detailReason; // 상세 탈퇴 사유

    @Column(nullable = false)
    private LocalDateTime withdrawalDate; // 탈퇴 시점
}
