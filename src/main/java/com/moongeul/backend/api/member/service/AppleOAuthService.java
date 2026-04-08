package com.moongeul.backend.api.member.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moongeul.backend.api.member.dto.AccessTokenResponseDTO;
import com.moongeul.backend.api.member.dto.AppleInfoResponseDTO;
import com.moongeul.backend.api.member.dto.ApplePublicKeysResponseDTO;
import com.moongeul.backend.api.member.dto.AppleTokenHeaderDTO;
import com.moongeul.backend.common.config.webclient.WebClientErrorHandler;
import com.moongeul.backend.common.exception.BadRequestException;
import com.moongeul.backend.common.exception.InternalServerException;
import com.moongeul.backend.common.exception.UnauthorizedException;
import com.moongeul.backend.common.response.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppleOAuthService {

    private static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    private static final String APPLE_REVOKE_URL = "https://appleid.apple.com/auth/revoke";
    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final long APPLE_CLIENT_SECRET_EXPIRE_SECONDS = 60L * 60L * 24L * 30L;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${oauth-config.apple.client-id:}")
    private String clientId;
    @Value("${oauth-config.apple.team-id:}")
    private String teamId;
    @Value("${oauth-config.apple.key-id:}")
    private String keyId;
    @Value("${oauth-config.apple.private-key:}")
    private String privateKey;

    @Value("${oauth-config.apple.local:}")
    private String localRedirectUri;
    @Value("${oauth-config.apple.deploy:}")
    private String deployRedirectUri;

    public AccessTokenResponseDTO getAppleToken(String code, String type) {
        validateAppleLoginConfig();

        String decodedCode = decodeAuthorizationCode(code);
        String redirectUri = "deploy".equalsIgnoreCase(type) ? deployRedirectUri : localRedirectUri;
        if (!StringUtils.hasText(redirectUri)) {
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", createAppleClientSecret());
        params.add("code", decodedCode);
        params.add("redirect_uri", redirectUri);

        log.info("Request Body: code={}, client_id={}, redirect_uri={}, grant_type={}",
                code, clientId, redirectUri, "authorization_code");

        AccessTokenResponseDTO tokenResponse = webClient.post()
                .uri(APPLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> WebClientErrorHandler.handleApiError(res, "getAppleToken"))
                .bodyToMono(AccessTokenResponseDTO.class)
                .block();

        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.getIdToken())) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }

        return tokenResponse;
    }

    public AppleInfoResponseDTO getAppleUserInfo(String idToken) {

        if (!StringUtils.hasText(idToken)) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }

        AppleTokenHeaderDTO tokenHeader = parseIdTokenHeader(idToken);
        ApplePublicKeysResponseDTO.ApplePublicKey applePublicKey = getApplePublicKey(tokenHeader);
        Claims claims = parseAndValidateIdToken(idToken, applePublicKey);

        String socialId = claims.getSubject();
        if (!StringUtils.hasText(socialId)) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }

        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);

        if (!StringUtils.hasText(name) && StringUtils.hasText(email)) {
            int atIndex = email.indexOf('@');
            name = atIndex > 0 ? email.substring(0, atIndex) : email;
        }

        return AppleInfoResponseDTO.builder()
                .id(socialId)
                .email(email)
                .name(name)
                .build();
    }

    // Apple 연동 해제 로직
    public void revokeAppleToken(String appleRefreshToken) {
        validateAppleLoginConfig();

        if (!StringUtils.hasText(appleRefreshToken)) {
            throw new BadRequestException(ErrorStatus.INVALID_TOKEN_REQUEST.getMessage());
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", createAppleClientSecret());
        params.add("token", appleRefreshToken);
        params.add("token_type_hint", "refresh_token");

        webClient.post()
                .uri(APPLE_REVOKE_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> WebClientErrorHandler.handleApiError(res, "revokeAppleToken"))
                .bodyToMono(String.class)
                .block();
    }

    private String createAppleClientSecret() {
        PrivateKey applePrivateKey = getApplePrivateKey();
        Instant now = Instant.now();

        try {
            return Jwts.builder()
                    .header()
                    .keyId(keyId)
                    .and()
                    .issuer(teamId)
                    .subject(clientId)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(APPLE_CLIENT_SECRET_EXPIRE_SECONDS)))
                    .claim("aud", APPLE_ISSUER)
                    .signWith(applePrivateKey, Jwts.SIG.ES256)
                    .compact();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to generate Apple client secret", e);
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }
    }

    private PrivateKey getApplePrivateKey() {
        if (!StringUtils.hasText(privateKey)) {
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }

        String normalizedPrivateKey = privateKey
                .replace("\\n", "\n")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        try {
            byte[] keyBytes = Base64.getDecoder().decode(normalizedPrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("EC").generatePrivate(keySpec);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            log.error("Failed to parse Apple private key", e);
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }
    }

    private AppleTokenHeaderDTO parseIdTokenHeader(String idToken) {
        String[] tokenParts = idToken.split("\\.");
        if (tokenParts.length < 2) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }

        try {
            byte[] decodedHeader = Base64.getUrlDecoder().decode(tokenParts[0]);
            AppleTokenHeaderDTO tokenHeader = objectMapper.readValue(decodedHeader, AppleTokenHeaderDTO.class);

            if (!StringUtils.hasText(tokenHeader.getKid()) || !StringUtils.hasText(tokenHeader.getAlg())) {
                throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
            }

            return tokenHeader;
        } catch (IOException | IllegalArgumentException e) {
            log.error("Failed to parse Apple id_token header", e);
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }
    }

    private ApplePublicKeysResponseDTO.ApplePublicKey getApplePublicKey(AppleTokenHeaderDTO tokenHeader) {
        ApplePublicKeysResponseDTO applePublicKeys = webClient.get()
                .uri(APPLE_PUBLIC_KEYS_URL)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, res -> WebClientErrorHandler.handleApiError(res, "getApplePublicKey"))
                .bodyToMono(ApplePublicKeysResponseDTO.class)
                .block();

        if (applePublicKeys == null || applePublicKeys.getKeys() == null || applePublicKeys.getKeys().isEmpty()) {
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }

        return applePublicKeys.getKeys().stream()
                .filter(key -> tokenHeader.getKid().equals(key.getKid())
                        && tokenHeader.getAlg().equalsIgnoreCase(key.getAlg()))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage()));
    }

    private Claims parseAndValidateIdToken(String idToken, ApplePublicKeysResponseDTO.ApplePublicKey applePublicKey) {
        RSAPublicKey publicKey = convertToPublicKey(applePublicKey);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            validateIssuer(claims);
            validateAudience(claims);
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to validate Apple id_token", e);
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }
    }

    private RSAPublicKey convertToPublicKey(ApplePublicKeysResponseDTO.ApplePublicKey applePublicKey) {
        try {
            BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(applePublicKey.getN()));
            BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(applePublicKey.getE()));

            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(rsaPublicKeySpec);
        } catch (IllegalArgumentException | GeneralSecurityException e) {
            log.error("Failed to convert Apple public key", e);
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }
    }

    private void validateIssuer(Claims claims) {
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }
    }

    private void validateAudience(Claims claims) {
        Object audience = claims.get("aud");
        boolean isValidAudience = false;

        if (audience instanceof String audienceValue) {
            isValidAudience = clientId.equals(audienceValue);
        } else if (audience instanceof Collection<?> audienceValues) {
            isValidAudience = audienceValues.stream()
                    .map(String::valueOf)
                    .anyMatch(clientId::equals);
        }

        if (!isValidAudience) {
            throw new UnauthorizedException(ErrorStatus.AUTH_UNAUTHORIZED.getMessage());
        }
    }

    private String decodeAuthorizationCode(String code) {
        try {
            return URLDecoder.decode(code, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Apple authorization code URL decoding failed. Use raw code.", e);
            return code;
        }
    }

    private void validateAppleLoginConfig() {
        if (!StringUtils.hasText(clientId)
                || !StringUtils.hasText(teamId)
                || !StringUtils.hasText(keyId)) {
            throw new InternalServerException(ErrorStatus.SERVER_ERROR.getMessage());
        }
    }
}
