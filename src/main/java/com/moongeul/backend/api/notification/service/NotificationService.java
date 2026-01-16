package com.moongeul.backend.api.notification.service;

import com.google.firebase.messaging.*;
import com.moongeul.backend.api.notification.dto.DeviceTokenRequestDTO;
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

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    /* 토큰 등록/수정 */
    @Transactional
    public void registerOrUpdateToken(String email, DeviceTokenRequestDTO requestDTO) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));

        deviceTokenRepository.findByToken(requestDTO.getToken())
                .ifPresentOrElse(
                        token -> token.updateMember(member),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .member(member)
                                .token(requestDTO.getToken())
                                .platform(requestDTO.getPlatform())
                                .build())
                );
    }

    @Transactional
    public void send(Member receiver, Member actor, NotificationType notificationType, String message, Long relatedId) {
        Notifications notifications = Notifications.builder()
                .receiver(receiver)
                .actor(actor)
                .content(message)
                .type(notificationType)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        notificationRepository.save(notifications);

        List<DeviceToken> tokens = deviceTokenRepository.findAllByMemberId(receiver.getId());

        if (!tokens.isEmpty()) {
            for (DeviceToken deviceToken : tokens) {
                try {
                    sendMessage(deviceToken.getToken(), notificationType.getKey(), message);
                } catch (FirebaseMessagingException e) {
                    if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                        log.warn("유효하지 않은 토큰 삭제: {}", deviceToken.getToken());
                        deviceTokenRepository.delete(deviceToken);
                    } else {
                        log.error("FCM 에러 발생: {}", e.getMessage());
                    }
                } catch (Exception e) {
                    log.error("일반 전송 에러: {}", e.getMessage());
                }
            }
        }
    }

    /* 토큰을 가진 기기에 푸시 알림 전송 */
    public void sendMessage(String targetToken, String title, String body) throws FirebaseMessagingException {
        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder()
                        .setTitle(title) // 알림 타입
                        .setBody(body) // 알림 내용
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message); // 여기서 발생하는 에러가 위쪽(send 메서드)으로 전달
        log.info("FCM 전송 성공: " + response);
    }
}