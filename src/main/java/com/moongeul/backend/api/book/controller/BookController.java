package com.moongeul.backend.api.book.controller;

import com.moongeul.backend.api.book.dto.BookSearchRequestDTO;
import com.moongeul.backend.api.book.dto.BookSearchResponseDTO;
import com.moongeul.backend.api.book.service.BookService;
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

@Tag(name = "Book", description = "Book(도서) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/book")
@Validated
public class BookController {

    private final BookService bookService;

    @Operation(
            summary = "도서 검색 API",
            description = "네이버 도서 API를 활용하여 책 제목으로 도서를 검색합니다. 검색 결과는 DB에 저장되며, 이미 저장된 도서는 최신 정보로 업데이트됩니다. (페이지와 사이즈는 1부터 시작합니다)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "도서 검색 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 발생")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<BookSearchResponseDTO>> searchBooks(
            @RequestParam @NotBlank(message = "검색어는 필수입니다.") String query,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {
        
        BookSearchRequestDTO bookSearchRequestDTO = BookSearchRequestDTO.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();
        
        BookSearchResponseDTO bookSearchResponseDTO = bookService.searchBooks(bookSearchRequestDTO);
        return ApiResponse.success(SuccessStatus.SEARCH_BOOK_SUCCESS, bookSearchResponseDTO);
    }
}

