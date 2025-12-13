package com.moongeul.backend.common.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum SuccessStatus {

	/**
	 * 200
	 */
	SEND_HEALTH_CHECK_SUCCESS(HttpStatus.OK,"서버 상태 체크 성공"),
	SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
	GET_USERINFO_SUCCESS(HttpStatus.OK, "사용자 정보 조회 성공"),
	REISSUE_TOKEN_SUCCESS(HttpStatus.OK, "토큰 재발급 성공")

	/**
	 * 201
	 */

	;

	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return this.httpStatus.value();
	}
}