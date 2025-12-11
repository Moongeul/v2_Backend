package com.moongeul.backend.common.config.jwt;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/*
* JwtTokenProvider는 로그인 성공 시 사용자에게 액세스/리프레시 토큰을 발급하는 역할을 하며,
* 이후 모든 요청에서 이 토큰의 유효성(진짜?가짜?)을 검증하고 토큰 정보를 Spring Security 인증 객체로 변환하여 권한을 부여
*/

@Slf4j
@Component
public class JwtTokenProvider {
    private final SecretKey key;

    // application.yml에서 secret 값 가져와서 key에 저장
    // 1. 비밀 열쇠 보관 (생성자) : 토큰(신분증)을 만들고 검사할 때 쓰는 비밀 열쇠
    public JwtTokenProvider(@Value("${jwt.secretKey}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    // Member 정보를 가지고 AccessToken, RefreshToken을 생성하는 메서드
    // 2. 신분증 발급!
    public JwtTokenDTO generateToken(Member member) {

        // Member 객체에서 필요한 정보(권한, 이메일/이름)를 직접 추출합니다.
        String authorities = member.getAuthorityKey(); // Member Entity에 getAuthority()가 있다고 가정
        String subject = member.getEmail(); // 토큰의 subject는 Member의 이메일로 설정

        long now = (new Date()).getTime();

        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + accessTokenExpirationPeriod);
        String accessToken = Jwts.builder()
                .subject(subject) // 이메일
                .claim("auth", authorities) // 권한
                .expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .expiration(new Date(now + refreshTokenExpirationPeriod))
                .signWith(key)
                .compact();

        return JwtTokenDTO.builder()
                .grantType("Bearer") //JWT에 대한 인증 타입
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 토큰 정보를 검증하는 메서드
    // 3. 신분증 검사 : 제출하는 액세스 토큰이 진짜인지 확인
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    // 4. 정보 추출 및 권한 부여
    public Authentication getAuthentication(String accessToken) {
        // Jwt 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get("auth") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get("auth").toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication return
        // UserDetails: interface, User: UserDetails를 구현한 class
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }


    // accessToken
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}