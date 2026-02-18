package com.moongeul.backend.api.bookshelf.controller;

import com.moongeul.backend.api.bookshelf.dto.DoneReadCalendarResponseDTO;
import com.moongeul.backend.api.bookshelf.dto.DoneReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.service.DoneReadBookshelfService;
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
            description = "인증된 사용자의 읽은 책장 목록을 최신순으로 조회합니다. (페이지와 사이즈는 1부터 시작합니다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책장 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<DoneReadBookshelfResponseDTO>> getDoneReadBooks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {
        
        DoneReadBookshelfResponseDTO doneReadBookshelfResponseDTO = doneReadBookshelfService.getDoneReadBooks(userDetails.getUsername(), page, size);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_BOOKS_SUCCESS, doneReadBookshelfResponseDTO);
    }

    @Operation(
            summary = "읽은 책 캘린더 조회 API",
            description = "입력한 연/월 기준으로 일자별 읽은 책 정보를 조회합니다. " +
                    "같은 날짜에 여러 권이 있으면 가장 최근에 작성된 기록의 표지를 대표로 반환하고 count로 개수를 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽은 책 캘린더 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<DoneReadCalendarResponseDTO>> getDoneReadCalendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @Min(value = 1, message = "연도는 1 이상이어야 합니다.") Integer year,
            @RequestParam @Min(value = 1, message = "월은 1 이상이어야 합니다.") @Max(value = 12, message = "월은 12 이하여야 합니다.") Integer month) {

        DoneReadCalendarResponseDTO doneReadCalendar = doneReadBookshelfService.getDoneReadCalendar(
                userDetails.getUsername(), year, month);
        return ApiResponse.success(SuccessStatus.GET_DONE_READ_CALENDAR_SUCCESS, doneReadCalendar);
    }
}
