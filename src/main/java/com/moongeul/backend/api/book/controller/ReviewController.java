package com.moongeul.backend.api.book.controller;

import com.moongeul.backend.api.book.dto.ReviewRequestDTO;
import com.moongeul.backend.api.book.dto.ReviewResponseDTO;
import com.moongeul.backend.api.book.service.ReviewService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Book Review", description = "책 리뷰 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book/review")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(
            summary = "리뷰 작성 API",
            description = "도서에 대한 리뷰를 작성하는 API 입니다. 한 사용자는 한 도서에 대해 하나의 리뷰만 작성할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 리뷰를 작성한 도서입니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @PostMapping("/{isbn}")
    public ResponseEntity<ApiResponse<Void>> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn,
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {

        reviewService.createReview(isbn, reviewRequestDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.CREATE_BOOK_REVIEW_SUCCESS);
    }

    @Operation(
            summary = "리뷰 조회 API",
            description = "도서에 대한 리뷰를 최신순으로 조회하는 API 입니다. 전체 리뷰 평균 평점도 함께 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @GetMapping("/{isbn}")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviews(
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {

        ReviewResponseDTO response = reviewService.getReviews(isbn, page, size);
        return ApiResponse.success(SuccessStatus.GET_BOOK_REVIEWS_SUCCESS, response);
    }

    @Operation(
            summary = "리뷰 수정 API",
            description = "작성한 리뷰를 수정하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없습니다.")
    })
    @PutMapping("/{isbn}")
    public ResponseEntity<ApiResponse<Void>> updateReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn,
            @Valid @RequestBody ReviewRequestDTO reviewRequestDTO) {

        reviewService.updateReview(isbn, reviewRequestDTO, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.UPDATE_BOOK_REVIEW_SUCCESS);
    }

    @Operation(
            summary = "리뷰 삭제 API",
            description = "작성한 리뷰를 삭제하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 리뷰를 찾을 수 없습니다.")
    })
    @DeleteMapping("/{isbn}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn) {

        reviewService.deleteReview(isbn, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_BOOK_REVIEW_SUCCESS);
    }
}

