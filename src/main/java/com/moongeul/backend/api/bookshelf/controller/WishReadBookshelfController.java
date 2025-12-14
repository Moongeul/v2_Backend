package com.moongeul.backend.api.bookshelf.controller;

import com.moongeul.backend.api.bookshelf.dto.WishReadBookshelfRequestDTO;
import com.moongeul.backend.api.bookshelf.service.WishReadBookshelfService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WishReadBookshelf", description = "읽고 싶은 책장 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bookshelf/wish-read")
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
}
