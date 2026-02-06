package com.moongeul.backend.api.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpoPushRequestDTO {

    private List<String> to; // 수신자 토큰 리스트
    private String title;    // 알림 제목
    private String body;     // 알림 본문
    private String sound;    // "default"
    private Map<String, Object> pushData; // 추가 데이터
}
