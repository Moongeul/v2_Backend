package com.moongeul.backend.api.question.controller;

import com.moongeul.backend.api.question.dto.QuestionCreateRequestDTO;
import com.moongeul.backend.api.question.dto.QuestionDTO;
import com.moongeul.backend.api.question.dto.QuestionIdResponseDTO;
import com.moongeul.backend.api.question.dto.QuestionListResponseDTO;
import com.moongeul.backend.api.question.dto.QuestionModifyRequestDTO;
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

        // 비회원인 경우 "anonymousUser", 회원인 경우 email
        String username = (userDetails != null) ? userDetails.getUsername() : "anonymousUser";

        QuestionListResponseDTO questionListResponseDTO = questionService.getQuestionList(page, size, username);
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

    @Operation(
            summary = "질문 수정 API",
            description = "자신이 작성한 질문을 수정하는 API 입니다. 질문 내용과 책(ISBN)을 변경할 수 있습니다. 다른 사용자의 질문은 수정할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "질문 내용은 필수입니다 / ISBN은 필수입니다"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "질문 수정 권한이 없습니다"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 질문을 찾을 수 없습니다 / 해당 도서를 찾을 수 없습니다")
    })
    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponse<QuestionIdResponseDTO>> modifyQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionModifyRequestDTO questionModifyRequestDTO) {

        QuestionIdResponseDTO questionIdResponseDTO = questionService.modifyQuestion(questionId, questionModifyRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.MODIFY_QUESTION_SUCCESS, questionIdResponseDTO);
    }

    @Operation(
            summary = "질문 삭제 API",
            description = "자신이 작성한 질문을 삭제하는 API 입니다. 질문과 연관된 모든 답변도 함께 삭제됩니다. (하드삭제 - 추후 소프트삭제 고려해야함)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "질문 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "질문 삭제 권한이 없습니다"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 질문을 찾을 수 없습니다")
    })
    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long questionId) {

        questionService.deleteQuestion(questionId, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.DELETE_QUESTION_SUCCESS);
    }
}
