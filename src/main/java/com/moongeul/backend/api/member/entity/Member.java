package com.moongeul.backend.api.member.entity;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
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
@Table(name = "MEMBER") // 데이터베이스 테이블 이름 지정
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 회원번호(PK)

    @Column(unique = true, nullable = false)
    private String email; // 이메일

    private String name; // 회원 이름(실명)
    private String profileImage; // 회원 이미지
    private String nickname; //닉네임 (초기랜덤생성)
    private String password;

    @Enumerated(EnumType.STRING)
    private ReadingTasteType readingTasteType; // 독서 취향 유형

    @Enumerated(EnumType.STRING)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC; // 기본값: 전체공개

    @Column(unique = true)
    private String socialId; // 소셜 로그인 ID (고유값)
    
    private String socialType; // 소셜 로그인 제공자: Google, Kakao ...

    @Enumerated(EnumType.STRING)
    private Role role; // 권한, Role.valueOf(role)로 저장

    private String refreshToken; // Refresh Token

    @Column(length = 2000)
    private String socialRefreshToken; // OAuth Refresh Token

    @Builder.Default
    @Column(nullable = false)
    private boolean isPushEnabled = true; // 푸시알림 허용, 기본값: ON

    /**
     * 권한 가져오기
     */
    public String getAuthorityKey() {
        return this.role.getKey();
    }

    /**
     * OAuth2 로그인 시 이름, 사진이 변경될 경우 Entity를 업데이트하는 메서드
     */
    public Member update(String name) {
        this.name = name;
        return this;
    }

    /**
     * 리프레시 토큰 업데이트
     */
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * 소셜 리프레시 토큰 업데이트
     */
    public void updateSocialRefreshToken(String socialRefreshToken) {
        this.socialRefreshToken = socialRefreshToken;
    }

    /**
     * 독서 취향 유형 업데이트
     */
    public void updateReadingTasteType(ReadingTasteType readingTasteType) {
        this.readingTasteType = readingTasteType;
    }

    /**
     * 닉네임 업데이트
     */
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 프로필 이미지 업데이트
     */
    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * 공개 범위 변경 메서드
     */
    public void updatePrivacy(PrivacyLevel level) {
        this.privacyLevel = level;
    }

    /**
     * 푸시 알림 허용 업데이트
     */
    public void updatePushEnabled(boolean isPushEnabled) {
        this.isPushEnabled = isPushEnabled;
    }

    /**
     * 푸시 알림 허용 업데이트
     */
    public void updateRole(Role role) { this.role = role; }

    /**
     * 회원 탈퇴 시 개인정보를 지우는 메서드
     */
    public void withdrawMember() {
        this.socialId = null;        // 재가입 가능하도록 null 처리
        this.refreshToken = null;
        this.socialRefreshToken = null;
        this.nickname = "(알 수 없음)";
        this.profileImage = null;
        this.name = null;            // 실명 정보 삭제
        this.email = "withdrawn_" + this.id + "@moongeul.com"; // 이메일 중복 방지를 위해 식별 가능한 값으로 변경
        this.role = Role.GUEST;      // 권한 축소
    }
}
