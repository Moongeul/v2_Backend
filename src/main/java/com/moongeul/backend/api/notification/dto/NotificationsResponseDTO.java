package com.moongeul.backend.api.notification.dto;

import com.moongeul.backend.api.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsResponseDTO {

    private Long id; // 알림 ID

    private Long relatedId; // 연관된 ID값 (게시글ID, 회원ID 등)
    private NotificationType notificationType; // 알림 타입 (LIKE, FOLLOW_OPEN ... 등)

    private String profileImage;
    private String content;
    private LocalDateTime created_at;
    
    private boolean isRead; // 읽음 처리
}
