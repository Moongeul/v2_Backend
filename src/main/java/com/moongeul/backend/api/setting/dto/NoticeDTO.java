package com.moongeul.backend.api.setting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeDTO {

    private Long noticeId;
    private String title;
    private String content;
    private LocalDate uploadDate; // 업로드 날짜
}
