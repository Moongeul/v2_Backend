package com.moongeul.backend.api.question.controller;

import com.moongeul.backend.api.question.dto.AnswerCreateRequestDTO;
import com.moongeul.backend.api.question.dto.AnswerIdResponseDTO;
import com.moongeul.backend.api.question.dto.AnswerListResponseDTO;
import com.moongeul.backend.api.question.service.AnswerService;
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

@Tag(name = "Answer", description = "Answer(답변) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/answer")
public class AnswerController {

    private final AnswerService answerService;

    @Operation(
            summary = "답변 생성 API",
            description = "질문에 대한 답변을 생성하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "답변 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "답변 내용은 필수입니다 / 질문 ID는 필수입니다"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 질문을 찾을 수 없습니다 / 사용자를 찾을 수 없습니다")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AnswerIdResponseDTO>> createAnswer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AnswerCreateRequestDTO answerCreateRequestDTO) {

        AnswerIdResponseDTO answerIdResponseDTO = answerService.createAnswer(answerCreateRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CREATE_ANSWER_SUCCESS, answerIdResponseDTO);
    }

    @Operation(
            summary = "답변 리스트 조회 API",
            description = "질문에 달린 답변 리스트를 페이징하여 조회하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "답변 리스트 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 질문을 찾을 수 없습니다")
    })
    @GetMapping("/list/{questionId}")
    public ResponseEntity<ApiResponse<AnswerListResponseDTO>> getAnswerList(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long questionId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        AnswerListResponseDTO answerListResponseDTO = answerService.getAnswerList(questionId, page, size, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_ANSWER_LIST_SUCCESS, answerListResponseDTO);
    }
}
