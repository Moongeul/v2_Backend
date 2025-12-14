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
	SEARCH_BOOK_SUCCESS(HttpStatus.OK, "도서 검색 성공"),
	ADD_WISH_READ_BOOK_SUCCESS(HttpStatus.OK, "읽고 싶은 책 등록 성공"),
	REMOVE_WISH_READ_BOOK_SUCCESS(HttpStatus.OK, "읽고 싶은 책 삭제 성공")

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