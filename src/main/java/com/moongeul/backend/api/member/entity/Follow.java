package com.moongeul.backend.api.member.entity;

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
@Table(name = "FOLLOW") // 데이터베이스 테이블 이름 지정
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    private Member following; // 팔로우 대상

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private Member follower; // 팔로우를 신청한 사람

    @Enumerated(EnumType.STRING)
    private FollowStatus followStatus; // 팔로우 승인 상태

    // 상태 변경 (승인 시 사용)
    public void accept() {
        this.followStatus = FollowStatus.ACCEPTED;
    }
}
