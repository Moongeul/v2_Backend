package com.moongeul.backend.api.setting.controller;

import com.moongeul.backend.api.setting.dto.AgreeTermsRequestDTO;
import com.moongeul.backend.api.setting.dto.PrivacyLevelResponseDTO;
import com.moongeul.backend.api.setting.dto.PrivacyLevelUpdateRequestDTO;
import com.moongeul.backend.api.setting.service.AgreeTermsSettingService;
import com.moongeul.backend.api.setting.service.PrivacySettingService;
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

@Tag(name = "Setting", description = "설정 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/setting")
public class SettingController {

    private final PrivacySettingService privacySettingService;
    private final AgreeTermsSettingService agreeTermsSettingService;

    @Operation(
            summary = "계정 공개 범위 조회 API",
            description = "토큰 인증된 사용자의 계정 공개 범위를 조회합니다." +
                    "<br><br>[enum] privacyLevel ->" +
                    "<br>- PUBLIC: 전체 공개" +
                    "<br>- FOLLOWER_ONLY: 팔로워에게만 공개" +
                    "<br>- PRIVATE: 비공개"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 공개 범위 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @GetMapping("/privacy-level")
    public ResponseEntity<ApiResponse<PrivacyLevelResponseDTO>> getPrivacyLevel(
            @AuthenticationPrincipal UserDetails userDetails) {

        PrivacyLevelResponseDTO response = privacySettingService.getPrivacyLevel(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_PRIVACY_LEVEL_SUCCESS, response);
    }

    @Operation(
            summary = "계정 공개 범위 수정 API",
            description = "계정 공개 범위를 수정합니다." +
                    "<br><br>[enum] privacyLevel ->" +
                    "<br>- PUBLIC: 전체 공개" +
                    "<br>- FOLLOWER_ONLY: 팔로워에게만 공개" +
                    "<br>- PRIVATE: 비공개"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "계정 공개 범위 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "공개 범위 값이 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다.")
    })
    @PutMapping("/privacy-level")
    public ResponseEntity<ApiResponse<PrivacyLevelResponseDTO>> updatePrivacyLevel(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PrivacyLevelUpdateRequestDTO request) {

        PrivacyLevelResponseDTO response = privacySettingService.updatePrivacyLevel(userDetails.getUsername(), request);
        return ApiResponse.success(SuccessStatus.UPDATE_PRIVACY_LEVEL_SUCCESS, response);
    }

    @Operation(
            summary = "이용약관동의 API",
            description = "이용약관 동의 여부를 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "닉네임 중복 체크 성공")
    })
    @PostMapping("/agree-terms")
    public ResponseEntity<ApiResponse<Void>> agreeToTerms(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AgreeTermsRequestDTO agreeTermsRequestDTO) {

        agreeTermsSettingService.agreeToTerms(userDetails.getUsername(), agreeTermsRequestDTO);
        return ApiResponse.success_only(SuccessStatus.TERMS_AGREE_SUCCESS);
    }
}
