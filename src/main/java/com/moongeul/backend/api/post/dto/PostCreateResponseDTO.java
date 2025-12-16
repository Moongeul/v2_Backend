package com.moongeul.backend.api.post.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostCreateResponseDTO {

    private Long postId; // 생성된 Post id
}
