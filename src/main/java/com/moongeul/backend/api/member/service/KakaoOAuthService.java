package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.AccessTokenResponseDTO;
import com.moongeul.backend.api.member.dto.KakaoInfoResponseDTO;
import com.moongeul.backend.common.config.webclient.WebClientErrorHandler;
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


@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USERINFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth-config.google.local}")
    private String localRedirectUri;
    @Value("${oauth-config.google.deploy}")
    private String deployRedirectUri;

    // Kakao 토큰 획득 로직
    public AccessTokenResponseDTO getKakaoToken(String code, String type){

        // HTTP Body 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);

        String redirectUri = "deploy".equalsIgnoreCase(type) ? deployRedirectUri : localRedirectUri;

        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return webClient.post()
                .uri(KAKAO_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> WebClientErrorHandler.handleApiError(res, "getKakaoToken"))
                .bodyToMono(AccessTokenResponseDTO.class)
                .block();
    }

    // Kakao 사용자 정보 획득 로직
    public KakaoInfoResponseDTO getKakaoUserInfo(String kakaoAccessToken){

        return webClient.get()
                .uri(KAKAO_USERINFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> WebClientErrorHandler.handleApiError(res, "getKakaoUserInfo"))
                .bodyToMono(KakaoInfoResponseDTO.class)
                .block(); // 동기 방식으로 결과 대기
    }
}
