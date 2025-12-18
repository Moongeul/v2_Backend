package com.moongeul.backend.api.category.dto;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.category.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequestDTO {

    @NotBlank(message = "카테고리명은 필수입니다")
    private String category; // 카테고리명

    public Category toEntity(Member member){

        return Category.builder()
                .title(this.category)
                .member(member)
                .build();
    }
}
