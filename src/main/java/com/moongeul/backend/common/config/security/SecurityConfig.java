package com.moongeul.backend.common.config.security;

import com.moongeul.backend.api.member.jwt.filter.JwtAuthenticationFilter;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
* Spring Boot 애플리케이션의 보안 설정을 담당하는 핵심 클래스 SecurityConfig
* JWT 기반의 무상태(Stateless) 인증 시스템을 구축하고, 애플리케이션의 접근 권한 규칙(인가)를 정의함
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 기존 보안 설정... (예: CSRF 비활성화, 인가 설정)
        http
                .csrf(csrf -> csrf.disable()) // 예시
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 무상태 설정
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())) //h2-console 화면 깨짐 방지(iframe 렌더링 오류)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/h2-console/**").permitAll()
                        .requestMatchers("/static/**", "/index.html", "/firebase-messaging-sw.js", "/favicon.ico").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/api-doc/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/api/v2/member/google/login", "/api/v2/member/kakao/login", "/api/v2/member/reissue-token").permitAll()
                        .requestMatchers("/api/v2/reading-taste", "/api/v2/reading-taste/total-count").permitAll()
                        .requestMatchers("/api/v2/book/bestseller").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/post/**").permitAll()
                        .anyRequest().authenticated()
                );

        // JWT 필터 추가: 요청 전에 토큰을 검사하도록 설정
        // 모든 요청이 컨트롤러에 도달하기 전에 JWT 토큰을 확인
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider), // JwtTokenProvider 주입
                UsernamePasswordAuthenticationFilter.class // UsernamePasswordAuthenticationFilter 이전에 실행
        );

        return http.build();
    }
}
