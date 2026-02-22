package com.moongeul.backend.api.setting.entity;

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
@Table(name = "TERMS") // 데이터베이스 테이블 이름 지정
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TermsType termsType; // 이용약관 타입

    private String version; //v1.0, v1.1, v2.0 ...
    private String content; // 약관 조항
    private boolean isRequired; // 필수 여부
}
