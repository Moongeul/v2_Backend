package com.moongeul.backend.api.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostVisibility {

    PUBLIC("ALL"), FOLLOWERS("FOLLOWER_ONLY"), PRIVATE("NONE");

    private final String key;
}
