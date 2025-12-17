package com.moongeul.backend.api.category.controller;

import com.moongeul.backend.api.category.dto.CategoryCreateRequestDTO;
import com.moongeul.backend.api.category.dto.CategoryListResponseDTO;
import com.moongeul.backend.api.category.dto.CategoryResponseDTO;
import com.moongeul.backend.api.category.service.CategoryService;
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

@Tag(name = "Category", description = "Category(카테고리-기록) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/category")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "내 카테고리 전체 조회 API",
            description = "사용자의 카테고리를 가져오는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리 전체 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "카테고리명은 필수입니다. (category)"),
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponseDTO>> getCategory(@AuthenticationPrincipal UserDetails userDetails) {

        CategoryListResponseDTO response = categoryService.getCategory(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_CATEGORY_SUCCESS, response);
    }

    @Operation(
            summary = "카테고리 생성 API",
            description = "기록에 대한 카테고리를 생성하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "카테고리 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "카테고리명은 필수입니다. (category)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 카테고리를 찾을 수 없습니다."),
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(@AuthenticationPrincipal UserDetails userDetails,
                                                                           @Valid @RequestBody CategoryCreateRequestDTO categoryCreateRequestDTO) {

        CategoryResponseDTO response = categoryService.createCategory(categoryCreateRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_CATEGORY_SUCCESS, response);
    }
}
