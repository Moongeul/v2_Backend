package com.moongeul.backend.api.post.dto;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.category.entity.Category;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.post.entity.PostVisibility;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {

    /* 드롭다운 - 선택 입력*/
    private PostVisibility postVisibility; // 공개 여부
    private Long categoryId; // 카테고리 번호

    /* 필수 입력 */
    @NotBlank(message = "ISBN은 필수입니다")
    private String isbn; // 책

    @NotNull(message = "독서 완료일은 필수입니다")
    @PastOrPresent(message = "독서 완료일은 미래일 수 없습니다")
    private LocalDate readDate; // 읽은 날짜

    /* 기본값 있는 필드 */
    @Min(value = 0, message = "평점은 0.0 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5.0 이하이어야 합니다.")
    private Double rating; // 평점

    @Positive(message = "페이지 수는 양수여야 합니다")
    private Integer page; // 페이지 수

    /* 선택 입력 */
    @Size(max = 3000, message = "내용은 5000자 이하로 작성해야 합니다.")
    private String content; // 감상평
    private List<QuoteRequestDTO> quotes; // 인상깊은구절

    @Getter
    @Builder
    public static class QuoteRequestDTO {
        private String quoteContent; // 인용문 내용
        private Integer pageNumber; // 페이지 번호
    }

    public Post toEntity(Category category, Member member, Book book) {
        
        // rating과 page가 null로 들어오면 기본값으로 대체
        Double finalRating = (this.rating != null) ? this.rating : 5.0;
        Integer finalPage = (this.page != null) ? this.page : 300;
        
        return Post.builder()
                .postVisibility(this.postVisibility)
                .category(category)
                .readDate(this.readDate)
                .rating(finalRating)
                .page(finalPage)
                .content(this.content)
                .member(member)
                .book(book)
                .build();
    }

}
