package com.moongeul.backend.api.member.controller;

import com.moongeul.backend.api.member.dto.TestRequestDTO;
import com.moongeul.backend.api.member.dto.TestResponseDTO;
import com.moongeul.backend.api.member.service.ReadingTasteService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ReadingTaste", description = "ReadingTaste(독서취향테스트) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/reading-taste")
public class ReadingTasteController {

    private final ReadingTasteService readingTasteService;


    @Operation(
            summary = "독서 취향 테스트 API",
            description = "독서 취향 테스트 결과를 계산하여 유형을 반환하는 API 입니다." +
                    "<br><br>요청 형식:" +
                    "<br>- answers: 질문 번호(1-12)와 답변(A or B)의 Map으로 전달바랍니다." +
                    "<br><br>**예시:**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"answers\": {\n" +
                    "    \"1\": \"A\",\n" +
                    "    \"2\": \"B\",\n" +
                    "    \"3\": \"A\",\n" +
                    "    \"4\": \"B\",\n" +
                    "    \"5\": \"A\",\n" +
                    "    \"6\": \"B\",\n" +
                    "    \"7\": \"A\",\n" +
                    "    \"8\": \"B\",\n" +
                    "    \"9\": \"A\",\n" +
                    "    \"10\": \"B\",\n" +
                    "    \"11\": \"A\",\n" +
                    "    \"12\": \"B\"\n" +
                    "}\n" +
                    "       "
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "독서 취향 테스트 결과 계산 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "답변이 비어있습니다.")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TestResponseDTO>> calculateReadingTasteType(@AuthenticationPrincipal UserDetails userDetails,
                                                                                  @RequestBody TestRequestDTO testRequestDTO) {

        TestResponseDTO response = readingTasteService.calculateReadingTasteType(testRequestDTO, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.CALCULATE_READING_TASTE_SUCCESS, response);
    }

}
