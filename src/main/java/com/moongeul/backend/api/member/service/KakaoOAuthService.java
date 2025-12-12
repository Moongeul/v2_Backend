package com.moongeul.backend.api.member.service;

import com.moongeul.backend.api.member.dto.AccessTokenResponseDTO;
import com.moongeul.backend.api.member.dto.KakaoInfoResponseDTO;
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
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    // Kakao 토큰 획득 로직
    public AccessTokenResponseDTO getKakaoToken(String code){

        // HTTP Body 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        return webClient.post()
                .uri(KAKAO_TOKEN_URL)
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

    // Kakao 사용자 정보 획득 로직
    public KakaoInfoResponseDTO getKakaoUserInfo(String kakaoAccessToken){

        return webClient.get()
                .uri(KAKAO_USERINFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + kakaoAccessToken)
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
                .bodyToMono(KakaoInfoResponseDTO.class)
                .block(); // 동기 방식으로 결과 대기
    }
}
