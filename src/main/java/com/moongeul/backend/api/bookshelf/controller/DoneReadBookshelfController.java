package com.moongeul.backend.api.bookshelf.controller;

import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadRatingSummaryResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.service.DoneReadBookshelfService;
import com.moongeul.backend.api.post.dto.CategoryPostListResponseDTO;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "DoneReadBookshelf", description = "읽은 책장 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bookshelf/done-read")
@Validated
public class DoneReadBookshelfController {

    private final DoneReadBookshelfService doneReadBookshelfService;

    @Operation(
            summary = "읽은 책장 전체 조회 API",
            description = "읽은 책장 목록을 최신순으로 조회합니다. userId 쿼리파라미터가 없으면 본인, 있으면 해당 사용자의 읽은 책장을 조회합니다. (페이지와 사이즈는 1부터 시작합니다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책장 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 사용자의 정보는 공개되지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<DoneReadBookshelfResponseDTO>> getDoneReadBooks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {
        
        DoneReadBookshelfResponseDTO doneReadBookshelfResponseDTO =
                doneReadBookshelfService.getDoneReadBooks(userDetails.getUsername(), userId, page, size);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_BOOKS_SUCCESS, doneReadBookshelfResponseDTO);
    }

    @Operation(
            summary = "읽은 책 캘린더 조회 API",
            description = "입력한 연/월 기준으로 일자별 읽은 책 정보를 조회합니다. userId 쿼리파라미터가 없으면 본인, 있으면 해당 사용자의 정보를 조회합니다. " +
                    "같은 날짜에 여러 권이 있으면 가장 최근에 작성된 기록의 표지를 대표로 반환하고 count로 개수를 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책 캘린더 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 사용자의 정보는 공개되지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<DoneReadCalendarResponseDTO>> getDoneReadCalendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam @Min(value = 1, message = "연도는 1 이상이어야 합니다.") Integer year,
            @RequestParam @Min(value = 1, message = "월은 1 이상이어야 합니다.") @Max(value = 12, message = "월은 12 이하여야 합니다.") Integer month) {

        DoneReadCalendarResponseDTO doneReadCalendar = doneReadBookshelfService.getDoneReadCalendar(
                userDetails.getUsername(), userId, year, month);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_CALENDAR_SUCCESS, doneReadCalendar);
    }

    @Operation(
            summary = "읽은 책 별점 요약 조회 API",
            description = "사용자가 기록한 총 책 수와 별점 구간(1.0~1.4, 1.5~1.9, ... , 4.5~5.0)별 기록 수를 조회합니다. userId 쿼리파라미터가 없으면 본인, 있으면 해당 사용자의 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책 별점 요약 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 사용자의 정보는 공개되지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/rating-summary")
    public ResponseEntity<ApiResponse<DoneReadRatingSummaryResponseDTO>> getDoneReadRatingSummary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId) {

        DoneReadRatingSummaryResponseDTO doneReadRatingSummaryResponseDTO =
                doneReadBookshelfService.getDoneReadRatingSummary(userDetails.getUsername(), userId);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_RATING_SUMMARY_SUCCESS, doneReadRatingSummaryResponseDTO);
    }

    @Operation(
            summary = "읽은 책 별점 구간 상세 조회 API",
            description = "별점 구간(range)에 해당하는 기록 리스트를 조회합니다. " +
                    "응답 형식은 카테고리별 기록 리스트 조회와 동일합니다." +
                    "<br><br>예시 range: 1.0~1.4, 1.5~1.9, ... , 4.5~5.0" +
                    "<br><br>[enum] 정렬 옵션 (sortBy):" +
                    "<br>- LATEST: 최신순 (기본값)" +
                    "<br>- OLDEST: 오래된순" +
                    "<br>- RATING_HIGH: 평점 높은순" +
                    "<br>- RATING_LOW: 평점 낮은순"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책 별점 구간 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 별점 구간입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "해당 사용자의 정보는 공개되지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/rating-summary/details")
    public ResponseEntity<ApiResponse<CategoryPostListResponseDTO>> getDoneReadRatingDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long userId,
            @RequestParam String range,
            @RequestParam(defaultValue = "LATEST") String sortBy,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {

        CategoryPostListResponseDTO categoryPostListResponseDTO =
                doneReadBookshelfService.getDoneReadRatingDetail(userDetails.getUsername(), userId, range, sortBy, page, size);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_RATING_DETAIL_SUCCESS, categoryPostListResponseDTO);
    }
}
