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

	/* MEMBER */
	SEND_LOGIN_SUCCESS(HttpStatus.OK, "로그인 성공"),
	GET_USERINFO_SUCCESS(HttpStatus.OK, "사용자 정보 조회 성공"),
	REISSUE_TOKEN_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),

	/* BOOK */
	SEARCH_BOOK_SUCCESS(HttpStatus.OK, "도서 검색 성공"),
	GET_BOOK_DETAIL_SUCCESS(HttpStatus.OK, "도서 상세 조회 성공"),
	
	/* BOOKSHELF */
	ADD_WISH_READ_BOOK_SUCCESS(HttpStatus.OK, "읽고 싶은 책 등록 성공"),
	REMOVE_WISH_READ_BOOK_SUCCESS(HttpStatus.OK, "읽고 싶은 책 삭제 성공"),

	/* POST */
	CREATE_POST_SUCCESS(HttpStatus.OK, "글쓰기 성공"),

	/* CATEGORY */
	CREATE_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리 생성 성공"),
	GET_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리 전체 조회 성공"),

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