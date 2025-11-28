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

    @Column(unique = true)
    private String socialId; // 소셜 로그인 ID (고유값)
    
    private String socialType; // 소셜 로그인 제공자: Google, Kakao ...

    @Enumerated(EnumType.STRING)
    private Role role; // 권한, Role.valueOf(role)로 저장

    private String refreshToken; // Refresh Token

    /**
     * 권한 가져오기
     */
    public String getAuthorityKey() {
        return this.role.getKey();
    }

    /**
     * OAuth2 로그인 시 이름, 사진이 변경될 경우 Entity를 업데이트하는 메서드
     */
    public Member update(String name, String picture) {
        this.name = name;
        this.profileImage = picture;
        return this;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
