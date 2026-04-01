package com.moongeul.backend.api.bookshelf.entity;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "done_read_bookshelf")
public class DoneReadBookshelf extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "done_bookshelf_id")
    private Long doneBookshelfId;

    @Column(name = "weight")
    private Float weight; // 도서 길이 기반 두께

    @Column(name = "height")
    private Float height; // 별점 기반 높낮이

    @Column(name = "post_count", nullable = false)
    @Builder.Default
    private Integer postCount = 1; // 해당 책에 대한 게시글 개수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Post article; // 가장 최근 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원

    // 읽은책장 업데이트 (가장 최근 게시글로 변경, 게시글 개수 증가)
    public void updateWithNewPost(Post newPost, Float weight, Float height) {
        this.article = newPost;
        this.weight = weight;
        this.height = height;
        this.postCount = this.postCount + 1;
    }

    // 게시글 삭제 시 이전 기록으로 갱신 (게시글 개수 감소 및 데이터 재설정)
    public void updateWithPreviousPost(Post previousPost, Float weight, Float height) {
        this.article = previousPost;
        this.weight = weight;
        this.height = height;
        this.postCount = Math.max(0, this.postCount - 1); // 개수 감소 (음수 방지)
    }
}

