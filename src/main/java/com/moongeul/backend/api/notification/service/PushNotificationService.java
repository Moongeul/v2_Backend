package com.moongeul.backend.api.notification.service;

import com.moongeul.backend.api.notification.dto.DeviceTokenRequestDTO;
import com.moongeul.backend.api.notification.dto.ExpoPushRequestDTO;
import com.moongeul.backend.api.notification.entity.DeviceToken;
import com.moongeul.backend.api.notification.entity.NotificationType;
import com.moongeul.backend.api.notification.entity.Notifications;
import com.moongeul.backend.api.notification.repository.DeviceTokenRepository;
import com.moongeul.backend.api.notification.repository.NotificationRepository;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    private final WebClient webClient;
    private static final String EXPO_PUSH_URL = "https://exp.host/--/api/v2/push/send";

    /* 토큰 등록/수정 */
    @Transactional
    public void registerOrUpdateToken(String email, DeviceTokenRequestDTO deviceTokenRequestDTO) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        // 1. 기존에 해당 토큰이 있는지 확인
        deviceTokenRepository.findByToken(deviceTokenRequestDTO.getToken())
                .ifPresentOrElse(
                        // 2. 이미 있다면: 토큰의 주인이 바뀌었을 수 있으므로 업데이트
                        existingToken -> existingToken.updateMember(member),
                        // 3. 없다면: 새로운 DeviceToken 생성 및 저장
                        () -> {
                            DeviceToken newToken = DeviceToken.builder()
                                    .member(member)
                                    .token(deviceTokenRequestDTO.getToken())
                                    .platform(deviceTokenRequestDTO.getPlatform())
                                    .build();
                            deviceTokenRepository.save(newToken);
                        }
                );
    }

    /* 알림 전송 */
    @Transactional
    public void send(Member receiver, Member actor, NotificationType notificationType, String message, Long relatedId) {
        // 1. 알림 내역 DB 저장
        Notifications notifications = Notifications.builder()
                .receiver(receiver)
                .actor(actor)
                .content(message)
                .type(notificationType)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        notificationRepository.save(notifications);

        // 2. 수신자의 모든 디바이스 토큰 조회
        List<String> tokenStrings = deviceTokenRepository.findAllByMemberId(receiver.getId())
                .stream()
                .map(DeviceToken::getToken)
                .collect(Collectors.toList());

        // Expo 전송용 추가 데이터 구성
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("type", notificationType);
        pushData.put("id", relatedId);

        if (!tokenStrings.isEmpty()) {
            // 3. WebClient로 비동기 전송
            sendToExpo(tokenStrings, notificationType.getKey(), message, pushData);
        }
    }

    /* Expo Push API 호출 */
    private void sendToExpo(List<String> targetTokens, String title, String body, Map<String, Object> pushData) {
        // 페이로드 구성
        ExpoPushRequestDTO requestPayload = ExpoPushRequestDTO.builder()
                .to(targetTokens)
                .title(title)
                .body(body)
                .sound("default")
                .pushData(pushData)
                .build();

        webClient.post()
                .uri(EXPO_PUSH_URL)
                .bodyValue(requestPayload)
                .retrieve()
                .bodyToMono(Map.class) // 응답을 Map 형태로 받음
                .subscribe(
                        response -> log.info("Expo 푸시 전송 성공: {}", response),
                        error -> log.error("Expo 푸시 전송 실패: {}", error.getMessage())
                );
    }

    /* 로그아웃 시, 토큰 삭제 */
    @Transactional
    public void removeDeviceToken(String token) {
        // 토큰이 존재할 경우에만 삭제 진행
        deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
            deviceTokenRepository.delete(deviceToken);
            log.info("로그아웃으로 인한 디바이스 토큰 삭제 완료: {}", token);
        });
    }
}