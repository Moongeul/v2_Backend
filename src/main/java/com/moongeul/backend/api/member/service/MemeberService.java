package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.UserInfoDTO;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.entity.Role;
import com.moongeul.backend.api.member.jwt.dto.JwtTokenDTO;
import com.moongeul.backend.api.member.dto.LoginResponseDTO;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.config.jwt.JwtTokenProvider;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemeberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate = new RestTemplate(); // Google 통신 객체

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    // 인가코드 받아 JWT로 교환 및 회원가입/로그인 처리
    @Transactional
    public LoginResponseDTO loginWithGoogle(String code){

        // 1. 인가 코드로 Google Access Token 및 사용자 정보 획득
        Map<String, String> tokenResponse = getGoogleToken(code);
        Map<String, Object> userInfo = getGoogleUserInfo(tokenResponse.get("access_token"));

        // 2. 사용자 정보 추출
        String socialId = (String) userInfo.get("sub");
        String name = (String) userInfo.get("name");
        String picture = (String) userInfo.get("picture");

        // 3. DB 처리 (회원가입 또는 로그인)
        Member member = memberRepository.findBySocialId(socialId)
                .map(entity -> entity.update(name, picture)) // 이미 있으면 정보 업데이트
                .orElseGet(() -> signUp(socialId, name, picture)); // 없으면 신규 회원가입

        // 4. 자체 JWT 토큰 생성 및 반환
        JwtTokenDTO jwtToken = jwtTokenProvider.generateToken(member);
        member.updateRefreshToken(jwtToken.getRefreshToken()); // 생성된 refreshToken DB 저장

        return LoginResponseDTO.builder()
                .role(member.getAuthorityKey())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }

    // 신규 회원가입 처리 로직 (DB 저장)
    private Member signUp(String socialId, String name, String picture) {
        Member newUser = Member.builder()
                .email(UUID.randomUUID() + "@socialUser.com")
                .name(name)
                .profileImage(picture)
                .password("OAuth Password") // 임시 패스워드
                .socialId(socialId) // 예시 사용자명 생성
                .socialType("google")
                .role(Role.USER)
                .build();
        return memberRepository.save(newUser);
    }

    // Google 토큰 획득 로직 (생략: RestTemplate을 사용해 POST 요청)
    private Map<String, String> getGoogleToken(String code) {

        String decodedCode;
        try {
            // 인코딩된 code 값(예: %2F)을 원래 값(/)으로 디코딩합니다.
            decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 디코딩 실패 시 원본 코드를 사용하거나, 예외를 다시 던집니다.
            log.error("URL Decoding Failed, using original code.", e);
            decodedCode = code;
        }

        // ... (실제 Google OAuth 2.0 /token 엔드포인트 통신 로직 구현 필요)
        // 1. HTTP Body에 전송할 파라미터를 담습니다.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", decodedCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code"); // 인가 코드를 토큰으로 교환함을 명시

        // 2. HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // x-www-form-urlencoded 형식 지정

        // 3. HttpEntity (Header + Body) 생성
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

        // 4. Google 토큰 엔드포인트 URL
        String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

        // 5. POST 요청 및 응답 받기
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    GOOGLE_TOKEN_URL,
                    HttpMethod.POST,
                    httpEntity,
                    Map.class
            );

            // 6. 응답에서 토큰 정보 추출
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                // Map<String, String>으로 형변환하여 반환
                return (Map<String, String>) responseEntity.getBody();
            }
        } catch (HttpClientErrorException.BadRequest e) { // 💡 Google 400 응답을 여기서 잡습니다.
            log.error("Google 400 Response Body: {}", e.getResponseBodyAsString());
            // 이 응답 본문에는 "invalid_grant"가 포함되어 있을 것입니다.
            throw new RuntimeException("Google OAuth token exchange failed.", e);
        } catch (Exception e) {
            // ... (기타 네트워크 오류 처리)
            throw new RuntimeException("Google OAuth token exchange failed.", e);
        }
        // 예외 발생 시 빈 맵 반환 (혹은 특정 예외 던지기)
        throw new RuntimeException("Failed to get Google Token.");
    }

    // Google 사용자 정보 획득 로직 (생략: RestTemplate을 사용해 GET 요청)
    private Map<String, Object> getGoogleUserInfo(String googleAccessToken) {
        String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

        // 1. HTTP Header에 Google Access Token을 담습니다.
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 2. GET 요청 및 응답 받기
        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return (Map<String, Object>) responseEntity.getBody();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve Google user info.", e);
        }

        throw new RuntimeException("Failed to get Google User Info.");
    }

    // 사용자 정보 조회
    public UserInfoDTO getUserInfo(Member member){

        return UserInfoDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .build();
    }

    @Transactional(readOnly = true)
    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
