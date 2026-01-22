package com.moongeul.backend.api.question.controller;

import com.moongeul.backend.api.question.dto.QuestionCreateRequestDTO;
import com.moongeul.backend.api.question.dto.QuestionDTO;
import com.moongeul.backend.api.question.dto.QuestionIdResponseDTO;
import com.moongeul.backend.api.question.dto.QuestionListResponseDTO;
import com.moongeul.backend.api.question.service.QuestionService;
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

@Tag(name = "Question", description = "Question(질문) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/question")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "질문 생성 API",
            description = "질문을 생성하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "질문 작성은 필수입니다"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 도서를 찾을 수 없습니다.")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<QuestionIdResponseDTO>> createQuestion(@AuthenticationPrincipal UserDetails userDetails,
                                                                         @Valid @RequestBody QuestionCreateRequestDTO questionCreateRequestDTO) {

        QuestionIdResponseDTO response = questionService.createQuestion(questionCreateRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_QUESTION_SUCCESS, response);
    }

    @Operation(
            summary = "질문 리스트 조회 API",
            description = "질문 리스트를 페이징하여 조회하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 리스트 조회 성공")
    })
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<QuestionListResponseDTO>> getQuestionList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        QuestionListResponseDTO questionListResponseDTO = questionService.getQuestionList(page, size, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_QUESTION_LIST_SUCCESS, questionListResponseDTO);
    }

    @Operation(
            summary = "질문 상세 조회 API",
            description = "특정 질문의 상세 정보를 조회하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 질문을 찾을 수 없습니다.")
    })
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionDTO>> getQuestionDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long questionId) {

        QuestionDTO questionDTO = questionService.getQuestionDetail(questionId, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_QUESTION_DETAIL_SUCCESS, questionDTO);
    }
}
