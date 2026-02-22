package com.moongeul.backend.api.readingTaste.controller;

import com.moongeul.backend.api.readingTaste.dto.TestRequestDTO;
import com.moongeul.backend.api.readingTaste.dto.TestResponseDTO;
import com.moongeul.backend.api.readingTaste.dto.TestStatisticsResponseDTO;
import com.moongeul.backend.api.readingTaste.service.ReadingTasteService;
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
                    "<br>- guestUuid: 비회원 식별을 위한 UUID입니다.(브라우저 로컬스토리지 저장용) 로그인 여부 상관없이, 로컬스토리지에 있는 guestUuid를 전달바랍니다." +
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

        // 비회원인 경우 "anonymousUser", 회원인 경우 email
        String username = (userDetails != null) ? userDetails.getUsername() : "anonymousUser";

        TestResponseDTO response = readingTasteService.calculateReadingTasteType(testRequestDTO, username);
        return ApiResponse.success(SuccessStatus.CALCULATE_READING_TASTE_SUCCESS, response);
    }

    @Operation(
            summary = "독서 취향 테스트 참여자 수 반환 API",
            description = "독서 취향 테스트 참여자 수를 반환하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "독서 취향 테스트 참여자 수 반환 성공"),
    })
    @GetMapping("/total-count")
    public ResponseEntity<ApiResponse<TestStatisticsResponseDTO>> getTotalCount() {

        TestStatisticsResponseDTO response = readingTasteService.getTotalParticipantsCount();
        return ApiResponse.success(SuccessStatus.GET_READING_TASTE_PARTICIPANTS_SUCCESS, response);
    }

    @Operation(
            summary = "비회원 독서 취향 테스트 결과 조회 및 로그인 연동 API",
            description = "회원가입 시, 비회원 상태에서 진행하였던 독서 취향 테스트 결과를 조회하여 결과가 있다면 로그인한 계정 정보에 저장하는 API 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "독서 취향 테스트 연동 성공"),
    })
    @PostMapping("/link")
    public ResponseEntity<ApiResponse<Void>> linkTestResult(@AuthenticationPrincipal UserDetails userDetails,
                                                                                @RequestParam String guestUuid) {

        readingTasteService.linkTestResult(guestUuid, userDetails.getUsername());
        return ApiResponse.success_only(SuccessStatus.LINK_READING_TASTE_SUCCESS);
    }
}
