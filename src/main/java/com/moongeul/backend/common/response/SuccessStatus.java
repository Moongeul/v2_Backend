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
	CALCULATE_READING_TASTE_SUCCESS(HttpStatus.OK, "독서 취향 테스트 결과 계산 성공"),
	FOLLOW_SUCCESS(HttpStatus.OK, "팔로우 성공"),

	/* BOOK */
	SEARCH_BOOK_SUCCESS(HttpStatus.OK, "도서 검색 성공"),
	GET_BOOK_DETAIL_SUCCESS(HttpStatus.OK, "도서 상세 조회 성공"),
	
	/* BOOK REVIEW */
	GET_BOOK_REVIEWS_SUCCESS(HttpStatus.OK, "책 리뷰 조회 성공"),
	UPDATE_BOOK_REVIEW_SUCCESS(HttpStatus.OK, "책 리뷰 수정 성공"),
	DELETE_BOOK_REVIEW_SUCCESS(HttpStatus.OK, "책 리뷰 삭제 성공"),
	
	/* BOOKSHELF */
	REMOVE_WISH_READ_BOOK_SUCCESS(HttpStatus.OK, "읽고 싶은 책 삭제 성공"),
	GET_WISH_READ_BOOKS_SUCCESS(HttpStatus.OK, "읽고 싶은 책장 조회 성공"),
	GET_DONE_READ_BOOKS_SUCCESS(HttpStatus.OK, "읽은 책장 조회 성공"),

	/* POST */
	GET_ALL_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 전체 조회 성공"),
	GET_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 상세 조회 성공"),
	UPDATE_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 수정 성공"),
	DELETE_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 삭제 성공"),
	POST_LIKE_SUCCESS(HttpStatus.OK, "기록(게시글) 공감 버튼 등록 성공"),

	/* CATEGORY */
	GET_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리 전체 조회 성공"),


	/**
	 * 201
	 */

	/* BOOK REVIEW */
	CREATE_BOOK_REVIEW_SUCCESS(HttpStatus.CREATED, "책 리뷰 작성 성공"),

	/* BOOKSHELF */
	ADD_WISH_READ_BOOK_SUCCESS(HttpStatus.CREATED, "읽고 싶은 책 등록 성공"),

	/* POST */
	CREATE_POST_SUCCESS(HttpStatus.CREATED, "글쓰기 성공"),

	/* CATEGORY */
	CREATE_CATEGORY_SUCCESS(HttpStatus.CREATED, "카테고리 생성 성공"),

	/* QUESTION */
	CREATE_QUESTION_SUCCESS(HttpStatus.CREATED, "질문 생성 성공"),
	;

	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return this.httpStatus.value();
	}
}