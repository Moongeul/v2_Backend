package com.moongeul.backend.api.setting.dto;

import com.moongeul.backend.api.post.entity.PostVisibility;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeAllRequestDTO {

    @Min(value = 1, message = "페이지는 1 이상이어야 합니다.")
    private Integer page = 1; // 페이지 번호 (기본값 1)

    @Min(value = 1, message = "한 페이지당 개수는 1 이상이어야 합니다.")
    private Integer size = 10; // 한 페이지당 개수 (기본값 10, 최대 100)
}
