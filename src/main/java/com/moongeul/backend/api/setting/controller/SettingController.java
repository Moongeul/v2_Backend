package com.moongeul.backend.api.setting.controller;

import com.moongeul.backend.api.setting.dto.InfoOpenResponseDTO;
import com.moongeul.backend.api.setting.dto.InfoOpenUpdateRequestDTO;
import com.moongeul.backend.api.setting.service.InfoOpenService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Setting", description = "설정 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/setting")
public class SettingController {

    private final InfoOpenService infoOpenService;

    @Operation(
            summary = "계정 공개 범위 조회 API",
            description = "토큰 인증된 사용자의 계정 공개 범위를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 공개 범위 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/info-open")
    public ResponseEntity<ApiResponse<InfoOpenResponseDTO>> getInfoOpen(
            @AuthenticationPrincipal UserDetails userDetails) {

        InfoOpenResponseDTO infoOpenResponseDTO = infoOpenService.getInfoOpen(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_INFO_OPEN_SUCCESS, infoOpenResponseDTO);
    }

    @Operation(
            summary = "계정 공개 범위 수정 API",
            description = "계정 공개 범위를 수정합니다. 세 값 중 하나만 true 여야 합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 공개 범위 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "공개 범위 값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PutMapping("/info-open")
    public ResponseEntity<ApiResponse<InfoOpenResponseDTO>> updateInfoOpen(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InfoOpenUpdateRequestDTO request) {

        InfoOpenResponseDTO infoOpenResponseDTO = infoOpenService.updateInfoOpen(userDetails.getUsername(), request);
        return ApiResponse.success(SuccessStatus.UPDATE_INFO_OPEN_SUCCESS, infoOpenResponseDTO);
    }
}
