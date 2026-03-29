package com.moongeul.backend.api.book.dto;

import com.moongeul.backend.api.readingTaste.entity.ReadingTasteType;
import com.moongeul.backend.api.post.dto.PostDTO;
import com.moongeul.backend.api.post.dto.QuoteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewItemDTO {
    private Long postId; // 게시글 ID
    private Long memberId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private ReadingTasteType readingTasteType; // 독서 취향
    private String profileImage; // 사용자 프로필 이미지
    private LocalDateTime createdAt; // 작성 일자
    private Double rating; // 별점
    private String content; // 내용
    private List<QuoteDTO> quotes; // 인상 깊은 구절들
    private PostDTO.LikesCnt likesCnt; // 받은 감정 상태들(공감 통계)
}
