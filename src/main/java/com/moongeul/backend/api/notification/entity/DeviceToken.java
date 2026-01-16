package com.moongeul.backend.api.notification.entity;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "device_token")
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 한 명당 여러 기기(토큰)를 가질 수 있으므로 1:N 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String token; // FCM에서 발급받은 디바이스 토큰

    private String platform; // Android 또는 iOS

    // 토큰 주인 업데이트 로직
    public void updateMember(Member member) {
        this.member = member;
    }
}
