package com.moongeul.backend.api.notification.controller;

import com.moongeul.backend.api.notification.dto.DeviceTokenRequestDTO;
import com.moongeul.backend.api.notification.dto.NotificationsResponseDTO;
import com.moongeul.backend.api.notification.dto.checkNotificationsResponseDTO;
import com.moongeul.backend.api.notification.service.NotificationService;
import com.moongeul.backend.api.notification.service.PushNotificationService;
import com.moongeul.backend.common.response.ApiResponse;
import com.moongeul.backend.common.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification", description = "Notification(푸시알람) 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notification")
public class NotificationController {

    private final PushNotificationService pushNotificationService;
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
        pushNotificationService.registerOrUpdateToken(userDetails.getUsername(), requestDTO);

        return ApiResponse.success_only(SuccessStatus.REGISTER_DEVICE_TOKEN_SUCCESS);
    }

    @Operation(
            summary = "미확인 알림 존재 여부 조회 API",
            description = "미확인 알림이 존재하는지 여부(true/false)를 알 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "미확인 알림 존재 여부 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다."),
    })
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<checkNotificationsResponseDTO>> checkUnReadNotifications(@AuthenticationPrincipal UserDetails userDetails) {

        checkNotificationsResponseDTO response = notificationService.checkUnReadNotifications(userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_UNREAD_NOTIFICATIONS_SUCCESS, response);
    }

    @Operation(
            summary = "알림 내역 전체 조회 API",
            description = "알림 페이지에 띄울 정보들을 전체 조회 합니다." +
                    "<br>profileImage 값이 null인 것은 '서버공지'인 경우 입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "알림 내역 전체 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 사용자를 찾을 수 없습니다."),
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationsResponseDTO>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.(1부터 시작)") Integer page,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.") Integer size) {

        List<NotificationsResponseDTO> response = notificationService.getNotifications(page, size, userDetails.getUsername());
        return ApiResponse.success(SuccessStatus.GET_NOTIFICATIONS_SUCCESS, response);
    }
}