package com.moongeul.backend.api.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostIdResponseDTO {

    private Long postId; // 생성된 Post id
}
