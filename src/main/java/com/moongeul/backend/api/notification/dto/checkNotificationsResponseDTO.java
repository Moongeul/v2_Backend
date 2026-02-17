package com.moongeul.backend.api.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class checkNotificationsResponseDTO {

    private boolean exist; // 읽지 않은 알림 존재 여부(true/false)
    private Long count; // 읽지 않은 알림 개수
}
