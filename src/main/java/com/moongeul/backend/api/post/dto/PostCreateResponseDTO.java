package com.moongeul.backend.api.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostCreateResponseDTO {

    private Long post_id; // 생성된 Post id
}
