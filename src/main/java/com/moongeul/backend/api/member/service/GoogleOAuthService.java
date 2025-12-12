package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.GoogleInfoResponseDTO;
import com.moongeul.backend.api.member.dto.AccessTokenResponseDTO;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.InternalServerException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuthService {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    // Google 토큰 획득 로직 (WebClient 방식으로 수정 - 비동기 방식 구현)
    public AccessTokenResponseDTO getGoogleToken(String code) {

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
        // HTTP Body에 전송할 파라미터 담기
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", decodedCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code"); // 인가 코드를 토큰으로 교환함을 명시

        return webClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .onStatus(status -> status.value() == 401, response -> {
                    throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
                })
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    throw new BadRequestException(ErrorStatus.INVALID_TOKEN_REQUEST.getMessage());
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
                })
                .bodyToMono(AccessTokenResponseDTO.class)
                .block();
    }

    // Google 사용자 정보 획득 로직 (생략: RestTemplate을 사용해 GET 요청)
    public GoogleInfoResponseDTO getGoogleUserInfo(String googleAccessToken) {

        return webClient.get()
                .uri(GOOGLE_USERINFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + googleAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(status -> status.value() == 401, response -> {
                    throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
                })
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    throw new BadRequestException(ErrorStatus.INVALID_INFO_REQUEST.getMessage());
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
                })
                .bodyToMono(GoogleInfoResponseDTO.class)
                .block(); // 동기 방식으로 결과 대기
    }
}
