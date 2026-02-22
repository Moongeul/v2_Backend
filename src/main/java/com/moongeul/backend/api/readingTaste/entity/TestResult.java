package com.moongeul.backend.api.readingTaste.entity;

import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder // 빌더 패턴 사용을 위한 롬복 애너테이션
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Table(name = "TEST_RESULT") // 데이터베이스 테이블 이름 지정
public class TestResult extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 비회원도 참여 가능하므로 필수값 아님 (null 가능)
    @Column(name = "member_id")
    private Long memberId;

    // 비회원 식별 UUID: 브라우저에서 생성해 보낸 값 저장
    @Column(nullable = false, length = 36)
    private String guestUuid;

    @Enumerated(EnumType.STRING)
    private ReadingTasteType readingTasteType;

    // 회원가입 후 데이터를 유저와 연결
    public void updateMemberId(Long memberId) {
        this.memberId = memberId;
    }
}
