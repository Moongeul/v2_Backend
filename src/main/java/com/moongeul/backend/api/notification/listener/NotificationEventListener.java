package com.moongeul.backend.api.notification.listener;

import com.moongeul.backend.api.notification.entity.NotificationType;
import com.moongeul.backend.api.notification.event.LikeNotificationEvent;
import com.moongeul.backend.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener { // 발행된 이벤트를 받아서 별도의 스레드에서 비동기적으로 알림을 저장하고 전송하는 클래스

    private final NotificationService notificationService;

    @Async // 별도의 스레드에서 실행되도록 설정
    @EventListener // LikeNotificationEvent가 발행되면 호출됨
    public void handleLikeNotification(LikeNotificationEvent event) {
        String message = event.actor().getNickname() + "님이 회원님의 기록에 공감했습니다.";

        // 실제 DB 저장 및 FCM 발송 로직 실행
        notificationService.send(
                event.receiver(),
                event.actor(),
                NotificationType.LIKE,
                message,
                event.post().getId()
        );
    }
}
