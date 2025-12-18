package com.moongeul.backend.api.bookshelf.controller;

import com.moongeul.backend.api.bookshelf.dto.WishReadBookshelfRequestDTO;
import com.moongeul.backend.api.bookshelf.dto.WishReadBookshelfResponseDTO;
import com.moongeul.backend.api.bookshelf.service.WishReadBookshelfService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WishReadBookshelf", description = "읽고 싶은 책장 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bookshelf/wish-read")
@Validated
public class WishReadBookshelfController {

    private final WishReadBookshelfService wishReadBookshelfService;

    @Operation(
            summary = "읽고 싶은 책 등록 API",
            description = "ISBN을 받아서 읽고 싶은 책장에 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽고 싶은 책 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 등록된 도서이거나 잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 또는 도서를 찾을 수 없습니다.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addWishReadBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WishReadBookshelfRequestDTO wishReadBookshelfRequestDTO) {
        
        wishReadBookshelfService.addWishReadBook(userDetails.getUsername(), wishReadBookshelfRequestDTO.getIsbn());
        return ApiResponse.success_only(SuccessStatus.ADD_WISH_READ_BOOK_SUCCESS);
    }

    @Operation(
            summary = "읽고 싶은 책 삭제 API",
            description = "ISBN을 받아서 읽고 싶은 책장에서 삭제합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽고 싶은 책 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 또는 도서를 찾을 수 없습니다.")
    })
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> removeWishReadBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WishReadBookshelfRequestDTO wishReadBookshelfRequestDTO) {
        
        wishReadBookshelfService.removeWishReadBook(userDetails.getUsername(), wishReadBookshelfRequestDTO.getIsbn());
        return ApiResponse.success_only(SuccessStatus.REMOVE_WISH_READ_BOOK_SUCCESS);
    }

    @Operation(
            summary = "읽고 싶은 책장 전체 조회 API",
            description = "사용자의 읽고 싶은 책장 목록을 최신순으로 조회합니다. (페이지와 사이즈는 1부터 시작합니다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "읽고 싶은 책장 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<WishReadBookshelfResponseDTO>> getWishReadBooks(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {
        
        WishReadBookshelfResponseDTO wishReadBookshelfResponseDTO = wishReadBookshelfService.getWishReadBooks(userDetails.getUsername(), page, size);
        return ApiResponse.success(SuccessStatus.GET_WISH_READ_BOOKS_SUCCESS, wishReadBookshelfResponseDTO);
    }
}
