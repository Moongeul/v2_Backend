package com.moongeul.backend.api.book.controller;

import com.moongeul.backend.api.book.dto.BookDTO;
import com.moongeul.backend.api.book.dto.BookSearchRequestDTO;
import com.moongeul.backend.api.book.dto.BookSearchResponseDTO;
import com.moongeul.backend.api.book.dto.BestsellerBookListResponseDTO;
import com.moongeul.backend.api.book.dto.BestsellerRegisterRequestDTO;
import com.moongeul.backend.api.book.service.BookService;
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

@Tag(name = "Book", description = "Book(도서) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book")
@Validated
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "도서/사용자 검색 API",
            description = "검색어로 도서/사용자를 검색합니다." +
                    "<br>- type=book: 도서만 검색" +
                    "<br>- type=user: 사용자만 검색" +
                    "<br>- type=all: 도서와 사용자 모두 검색" +
                    "<br><br>도서 검색 결과는 DB에 저장되며, 이미 저장된 도서는 최신 정보로 업데이트됩니다. (페이지와 사이즈는 1부터 시작합니다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "도서/사용자 검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/user/search")
    public ResponseEntity<ApiResponse<BookSearchResponseDTO>> searchBooks(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String query,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {
        
        BookSearchRequestDTO bookSearchRequestDTO = BookSearchRequestDTO.builder()
                .query(query)
                .type(type)
                .page(page)
                .size(size)
                .build();
        
        BookSearchResponseDTO bookSearchResponseDTO = bookService.searchBooks(bookSearchRequestDTO);
        return ApiResponse.success(SuccessStatus.SEARCH_BOOK_SUCCESS, bookSearchResponseDTO);
    }

    @Operation(
            summary = "도서 상세 조회 API",
            description = "ISBN을 받아서 DB에 등록된 도서의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "도서 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @GetMapping("/{isbn}")
    public ResponseEntity<ApiResponse<BookDTO>> getBookDetail(
            @PathVariable @NotBlank(message = "ISBN은 필수입니다.") String isbn) {
        
        BookDTO bookDTO = bookService.getBookDetail(isbn);
        return ApiResponse.success(SuccessStatus.GET_BOOK_DETAIL_SUCCESS, bookDTO);
    }

    @Operation(
            summary = "베스트셀러 도서 등록 API (관리자 전용)",
            description = "관리자가 베스트셀러 도서를 ISBN 기준으로 최대 10권까지 등록합니다. 기존 목록은 새 목록으로 전체 교체됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "베스트셀러 도서 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 ISBN 리스트 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자만 접근할 수 있습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "등록하려는 도서를 찾을 수 없습니다.")
    })
    @PutMapping("/bestseller")
    public ResponseEntity<ApiResponse<Void>> registerBestsellerBooks(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BestsellerRegisterRequestDTO bestsellerRegisterRequestDTO) {

        bookService.registerBestsellerBooks(userDetails.getUsername(), bestsellerRegisterRequestDTO);
        return ApiResponse.success_only(SuccessStatus.REGISTER_BESTSELLER_BOOK_SUCCESS);
    }

    @Operation(
            summary = "베스트셀러 도서 조회 API",
            description = "등록된 베스트셀러 도서를 조회합니다. (최대 10권)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "베스트셀러 도서 조회 성공")
    })
    @GetMapping("/bestseller")
    public ResponseEntity<ApiResponse<BestsellerBookListResponseDTO>> getBestsellerBooks() {

        BestsellerBookListResponseDTO bestsellerBookListResponseDTO = bookService.getBestsellerBooks();
        return ApiResponse.success(SuccessStatus.GET_BESTSELLER_BOOK_SUCCESS, bestsellerBookListResponseDTO);
    }
}
