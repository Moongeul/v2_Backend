package com.moongeul.backend.api.story.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryDTO {

    private MemberInfo memberInfo;
    private StoryInfo storyInfo;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String nickname; // 닉네임
        private String profileImage; // 회원 이미지
        private ReadingTasteType readingTasteType; // 독서 취향 유형
    }

    @Getter
    @Builder
    public static class StoryInfo {
        private Long storyId;
        private String storyImage;
        private LocalDateTime created; // 작성 시간
    }
}
