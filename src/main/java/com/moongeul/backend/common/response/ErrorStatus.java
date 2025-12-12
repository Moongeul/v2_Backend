package com.moongeul.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)

public enum ErrorStatus {
    /**
     * 400 BAD_REQUEST
     */
    VALIDATION_REQUEST_MISSING_EXCEPTION(HttpStatus.BAD_REQUEST, "요청 값이 입력되지 않았습니다."),
    USER_ALREADY_EXISTS_EXCEPTION(HttpStatus.BAD_REQUEST,"이미 존재하는 사용자입니다."),
    MISSING_GOOGLE_ACCESSTOKEN(HttpStatus.BAD_REQUEST, "구글 엑세스토큰이 입력되지 않았습니다."),
    MISSING_KAKAO_ACCESSTOKEN(HttpStatus.BAD_REQUEST, "카카오 엑세스토큰이 입력되지 않았습니다."),
    INVALID_TOKEN_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 토큰 요청입니다."),
    INVALID_INFO_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 로그인 인증 요청입니다."),

    /**
     * 401 UNAUTHORIZED
     */
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 인가코드 입니다."),

    /**
     * 404 NOT_FOUND
     */
    USER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND,"해당 사용자를 찾을 수 없습니다."),

    /**
     * 500 SERVER_ERROR
     */
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"서버 내부 오류 발생"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 서버 오류 발생")

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}