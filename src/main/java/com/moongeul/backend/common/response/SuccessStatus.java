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
	GET_POST_STATS_SUCCESS(HttpStatus.OK, "기록 통계 조회 성공"),
	REISSUE_TOKEN_SUCCESS(HttpStatus.OK, "토큰 재발급 성공"),
	FOLLOW_SUCCESS(HttpStatus.OK, "팔로우 성공"),
	UNFOLLOW_SUCCESS(HttpStatus.OK, "언팔로우 성공"),
	GET_FOLLOWING_SUCCESS(HttpStatus.OK, "팔로잉 목록 조회 성공"),
	GET_FOLLOWER_SUCCESS(HttpStatus.OK, "팔로워 목록 조회 성공"),
	FOLLOW_PROCESS_SUCCESS(HttpStatus.OK, "팔로우 승인/삭제 성공"),
	REGENERATE_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 재생성 성공"),
	UPDATE_NICKNAME_SUCCESS(HttpStatus.OK, "닉네임 등록 성공"),
	UPDATE_PROFILE_IMAGE_SUCCESS(HttpStatus.OK, "프로필 이미지 변경 성공"),
	CHECK_NICKNAME_DUPLICATE_SUCCESS(HttpStatus.OK, "닉네임 중복 체크 성공"),
	GET_PRIVACY_LEVEL_SUCCESS(HttpStatus.OK, "계정 공개 범위 조회 성공"),
	UPDATE_PRIVACY_LEVEL_SUCCESS(HttpStatus.OK, "계정 공개 범위 수정 성공"),
	TERMS_AGREE_SUCCESS(HttpStatus.OK, "약관 동의 성공"),

	/* READING TASTE */
	CALCULATE_READING_TASTE_SUCCESS(HttpStatus.OK, "독서 취향 테스트 결과 계산 성공"),
	GET_READING_TASTE_PARTICIPANTS_SUCCESS(HttpStatus.OK, "독서 취향 테스트 참여자 수 반환 성공"),
	LINK_READING_TASTE_SUCCESS(HttpStatus.OK, "독서 취향 테스트 연동 성공"),

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
	GET_DONE_READ_CALENDAR_SUCCESS(HttpStatus.OK, "읽은 책 캘린더 조회 성공"),
	GET_DONE_READ_RATING_SUMMARY_SUCCESS(HttpStatus.OK, "읽은 책 별점 요약 조회 성공"),
	GET_DONE_READ_RATING_DETAIL_SUCCESS(HttpStatus.OK, "읽은 책 별점 구간 상세 조회 성공"),

	/* POST */
	GET_ALL_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 전체 조회 성공"),
	GET_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 상세 조회 성공"),
	GET_CATEGORY_POST_LIST_SUCCESS(HttpStatus.OK, "카테고리별 기록 리스트 조회 성공"),
	UPDATE_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 수정 성공"),
	DELETE_POST_SUCCESS(HttpStatus.OK, "기록(게시글) 삭제 성공"),
	POST_LIKE_SUCCESS(HttpStatus.OK, "기록(게시글) 공감 버튼 등록 성공"),
	GET_WEEKLY_RECOMMENDATION_SUCCESS(HttpStatus.OK, "주간 추천 기록 조회 성공"),
	GET_MOST_RECORDED_BOOK_SUCCESS(HttpStatus.OK, "가장 많이 기록된 책 조회 성공"),
	GET_WRITING_GUIDE(HttpStatus.OK, "글쓰기 도움 받기 조회 성공"),

	/* CATEGORY */
	GET_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리 전체 조회 성공"),

	/* ALARM */
	REGISTER_DEVICE_TOKEN_SUCCESS(HttpStatus.OK, "사용자 기기 토큰 등록 성공"),
	GET_NOTIFICATIONS_SUCCESS(HttpStatus.OK, "알림 내역 전체 조회 성공"),
	GET_UNREAD_NOTIFICATIONS_SUCCESS(HttpStatus.OK, "미확인 알림 존재 여부 조회 성공"),

	/* QUESTION */
	GET_QUESTION_LIST_SUCCESS(HttpStatus.OK, "질문 리스트 조회 성공"),
	GET_MY_QUESTION_LIST_SUCCESS(HttpStatus.OK, "마이페이지 질문 리스트 조회 성공"),
	GET_QUESTION_DETAIL_SUCCESS(HttpStatus.OK, "질문 상세 조회 성공"),
	MODIFY_QUESTION_SUCCESS(HttpStatus.OK, "질문 수정 성공"),
	DELETE_QUESTION_SUCCESS(HttpStatus.OK, "질문 삭제 성공"),


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

	/* ANSWER */
	CREATE_ANSWER_SUCCESS(HttpStatus.CREATED, "답변 생성 성공"),
	GET_ANSWER_LIST_SUCCESS(HttpStatus.OK, "답변 리스트 조회 성공"),
	MODIFY_ANSWER_SUCCESS(HttpStatus.OK, "답변 수정 성공"),
	DELETE_ANSWER_SUCCESS(HttpStatus.OK, "답변 삭제 성공"),
	;

	private final HttpStatus httpStatus;
	private final String message;

	public int getStatusCode() {
		return this.httpStatus.value();
	}
}
