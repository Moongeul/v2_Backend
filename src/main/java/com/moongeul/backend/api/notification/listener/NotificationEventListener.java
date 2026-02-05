package com.moongeul.backend.api.notification.listener;

import com.moongeul.backend.api.member.entity.PrivacyLevel;
import com.moongeul.backend.api.notification.entity.NotificationType;
import com.moongeul.backend.api.notification.event.AnswerNotificationEvent;
import com.moongeul.backend.api.notification.event.FollowNotificationEvent;
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

    /* 공감 알림 */
    @Async // 별도의 스레드에서 실행되도록 설정
    @EventListener // LikeNotificationEvent가 발행되면 호출됨
    public void handleLikeNotification(LikeNotificationEvent event) {
        String message = event.actor().getNickname() + "님이 회원님의 기록에 공감했습니다.";

        // 실제 DB 저장 및 Expo 푸시 알림 발송 로직 실행
        notificationService.send(
                event.receiver(),
                event.actor(),
                NotificationType.LIKE,
                message,
                event.post().getId()
        );
    }

    /* 댓글 알림 */
    @Async
    @EventListener
    public void handleCommentNotification(AnswerNotificationEvent event) {
        String message = event.actor().getNickname() + "님이 회원님의 질문에 댓글을 달았습니다.";

        // 실제 DB 저장 및 Expo 푸시 알림 발송 로직 실행
        notificationService.send(
                event.receiver(),
                event.actor(),
                NotificationType.LIKE,
                message,
                event.question().getId()
        );
    }

    /* 팔로우 알림 */
    @Async
    @EventListener
    public void handleFollowNotification(FollowNotificationEvent event) {

        String message = event.actor().getNickname() + "님이 회원님에게 팔로우를 요청했습니다.";
        NotificationType notificationType = NotificationType.FOLLOW_PRIVATE;

        if(event.receiver().getPrivacyLevel() == PrivacyLevel.PUBLIC){ // 공개 계정일 경우 (요청x)
            message = event.actor().getNickname() + "님이 회원님을 팔로우 했습니다.";
            notificationType = NotificationType.FOLLOW_OPEN;
        }

        // 실제 DB 저장 및 Expo 푸시 알림 발송 로직 실행
        notificationService.send(
                event.receiver(),
                event.actor(),
                notificationType,
                message,
                event.actor().getId()
        );
    }
}
