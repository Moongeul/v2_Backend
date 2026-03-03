package com.moongeul.backend.api.notification.dto;

import com.moongeul.backend.api.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsResponseDTO {

    private Long total; // 전체 검색 결과 수
    private Integer page; // 현재 페이지
    private Integer size; // 페이지당 개수
    private Integer totalPages; // 전체 페이지 수
    private Boolean isLast; // 마지막 페이지 여부
    private List<NotificationInfo> data; // 알림 리스트

    @Getter
    @Builder
    public static class NotificationInfo {
        private Long id; // 알림 ID

        private Long relatedId; // 연관된 ID값 (게시글ID, 회원ID 등)
        private NotificationType notificationType; // 알림 타입 (LIKE, FOLLOW_OPEN ... 등)

        private String profileImage;
        private String content;
        private LocalDateTime createdAt;

        private boolean isRead; // 읽음 처리
    }
}
