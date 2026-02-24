package com.moongeul.backend.api.book.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchUserDTO {

    private Long userId; // 사용자 ID
    private String profileImage; // 사용자 프로필 이미지
    private String nickname; // 사용자 닉네임
    private ReadingTasteType readingTasteType; // 사용자 독서 취향
}
