package com.moongeul.backend.api.member.dto;

import com.moongeul.backend.api.member.entity.FollowStatus;
import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowResponseDTO {

    private final Long id;
    private final String profileImage;
    private final String nickname;
    private final ReadingTasteType readingTasteType;
    private final FollowStatus myFollowStatus;
}
