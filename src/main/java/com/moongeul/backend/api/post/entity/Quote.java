package com.moongeul.backend.api.post.entity;

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
@Table(name = "QUOTE") // 데이터베이스 테이블 이름 지정
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 인상깊은구절 id

    private String quoteContent; // 인상깊은구절 내용
    private int pageNumber; // 페이지 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}
