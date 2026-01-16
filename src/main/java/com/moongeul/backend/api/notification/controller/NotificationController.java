package com.moongeul.backend.api.notification.controller;

import com.moongeul.backend.api.notification.dto.DeviceTokenRequestDTO;
import com.moongeul.backend.api.notification.service.NotificationService;
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

@Tag(name = "Notification", description = "Notification(푸시알람) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "사용자 기기 토큰 등록 API",
            description = "현재 로그인한 사용자의 기기 토큰을 DB에 저장하거나, 이미 있다면 최신 상태로 업데이트합니다." +
                    "<br><br>[enum] 토큰 플랫폼 유형 ->" +
                    "<br>- ANDROID: 안드로이드" +
                    "<br>- IOS: ios" +
                    "<br>- WEB: 웹"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용자 기기 토큰 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다."),
    })
    @PostMapping("/device-token")
    public ResponseEntity<ApiResponse<Void>> registerToken(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody DeviceTokenRequestDTO requestDTO) {
        notificationService.registerOrUpdateToken(userDetails.getUsername(), requestDTO);

        return ApiResponse.success_only(SuccessStatus.REGISTER_DEVICE_TOKEN);
    }
}