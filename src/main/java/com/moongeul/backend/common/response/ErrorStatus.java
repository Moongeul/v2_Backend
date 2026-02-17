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

    EMPTY_TEST_ANSWERS(HttpStatus.BAD_REQUEST, "답변이 비어있습니다."),
    INCOMPLETE_TEST_ANSWERS(HttpStatus.BAD_REQUEST, "12개 질문에 모두 답변해야 합니다. (현재: %d개)"),
    INVALID_QUESTION_NUMBER(HttpStatus.BAD_REQUEST, "테스트에 잘못된 questionNo(질문번호)가 입력되었습니다. (질문번호: %d)"),
    INVALID_ANSWER_VALUE(HttpStatus.BAD_REQUEST, "테스트 답변은 A 또는 B여야 합니다. (질문번호: %d, 현재답변: %s)"),
    NO_SCORE_RESULT(HttpStatus.BAD_REQUEST, "테스트 점수 계산 결과가 없습니다."),
    REVIEW_UNAUTHORIZED(HttpStatus.BAD_REQUEST, "수정하려는 회원의 리뷰가 아닙니다."),
    DISAGREE_REQUIRED_TERM(HttpStatus.BAD_REQUEST, "필수 약관에 모두 동의해야 합니다."),

    /**
     * 401 UNAUTHORIZED
     */
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "유효하지 않은 인가코드 입니다."),
    TOKEN_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었거나 유효하지 않은 토큰입니다."),
    POST_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "회원의 게시글이 아닙니다."),
    CATEGORY_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "회원의 카테고리가 아닙니다."),
    QUESTION_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "질문 수정 권한이 없습니다."),
    ANSWER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "답변 수정 권한이 없습니다."),

    /**
     * 404 NOT_FOUND
     */
    USER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND,"해당 사용자를 찾을 수 없습니다."),
    BOOK_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 도서를 찾을 수 없습니다."),
    POST_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 기록(게시글)을 찾을 수 없습니다."),
    CATEGORY_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 카테고리를 찾을 수 없습니다."),
    REVIEW_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 리뷰를 찾을 수 없습니다."),
    USER_READING_TASTE_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "사용자의 독서 취향 정보를 찾을 수 없습니다."),
    WEEKLY_RECOMMENDATION_NOT_FOUND_EXCEPTION(HttpStatus.NOT_FOUND, "주간 추천 기록을 찾을 수 없습니다."),
    QUESTION_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 질문을 찾을 수 없습니다."),
    ANSWER_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "해당 답변을 찾을 수 없습니다."),
    TERMS_NOTFOUND_EXCEPTION(HttpStatus.NOT_FOUND, "약관 정보를 찾을 수 없습니다."),

    /**
     * 400 BAD_REQUEST
     */
    BOOK_ALREADY_ADDED_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 읽고 싶은 책으로 등록된 도서입니다."),
    REVIEW_ALREADY_EXISTS_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 리뷰를 작성한 도서입니다."),
    NICKNAME_ALREADY_EXISTS_EXCEPTION(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다."),
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "자기 자신은 팔로우할 수 없습니다."),
    NO_FOLLOW_RELATIONSHIP(HttpStatus.BAD_REQUEST, "팔로우 관계가 존재하지 않습니다."),
    EXISTS_FOLLOW_ACCEPTED(HttpStatus.BAD_REQUEST, "이미 팔로우하고 있는 사용자입니다."),
    EXISTS_FOLLOW_PENDING(HttpStatus.BAD_REQUEST, "이미 팔로우 요청을 보냈습니다. 승인을 기다려주세요."),
    INVALID_RATING_RANGE_EXCEPTION(HttpStatus.BAD_REQUEST, "유효하지 않은 별점 구간입니다."),
    PRIVACY_FORBIDDEN_EXCEPTION(HttpStatus.FORBIDDEN, "해당 사용자의 정보는 공개되지 않습니다."),

    /**
     * 500 SERVER_ERROR
     */
    INTERNAL_SERVER_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR,"서버 내부 오류 발생"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 서버 오류 발생"),
    NAVER_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 서버 오류 발생"),

    ;

    private final HttpStatus httpStatus;
    private final String message;

    public int getStatusCode() {
        return this.httpStatus.value();
    }
}
