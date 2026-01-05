package com.moongeul.backend.api.book.controller;

import com.moongeul.backend.api.book.dto.ReviewResponseDTO;
import com.moongeul.backend.api.book.service.ReviewService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Book Review", description = "책 리뷰 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book/review/{isbn}")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "도서 리뷰 조회 API",
            description = "도서에 대한 게시글(리뷰)을 최신순으로 조회하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviews(
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {

        ReviewResponseDTO response = reviewService.getReviews(isbn, page, size);
        return ApiResponse.success(SuccessStatus.GET_BOOK_REVIEWS_SUCCESS, response);
    }
}

