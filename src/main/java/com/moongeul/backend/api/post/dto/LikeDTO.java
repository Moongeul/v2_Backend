package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.post.entity.Likes;
import com.moongeul.backend.api.post.entity.LikeType;
import com.moongeul.backend.api.post.entity.Post;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeDTO {

    @NotNull(message = "공감 유형은 필수 입니다.")
    private LikeType likeType; // 공감 유형

    public Likes toEntity(Member member, Post post){
        return Likes.builder()
                .member(member)
                .post(post)
                .likeType(this.likeType)
                .build();
    }
}
