package com.moongeul.backend.api.setting.dto;

import com.moongeul.backend.api.setting.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeRequestDTO {

    private String title;
    private String content;

    public Notice toEntity(){
        return Notice.builder()
                .content(this.content)
                .title(this.title)
                .build();
    }
}
