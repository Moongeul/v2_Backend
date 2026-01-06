package com.moongeul.backend.api.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LikeType {

    RELATABLE("공감돼요"),
    SAME_TASTE("취향이 같아요"),
    IMPRESSIVE_EXPRESSION("표현이 인상적이에요"),
    WANT_TO_READ("읽고 싶네요"),
    HELPFUL("도움이 됐어요");

    private final String key;
}
