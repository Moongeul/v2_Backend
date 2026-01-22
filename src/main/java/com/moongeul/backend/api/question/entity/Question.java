package com.moongeul.backend.api.question.entity;

import com.moongeul.backend.api.book.entity.Book;
import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder // 빌더 패턴 사용을 위한 롬복 애너테이션
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Table(name = "QUESTION") // 데이터베이스 테이블 이름 지정
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 id

    private String content; // 질문 내용
    private Integer commentCnt; // 댓글 개수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false)
    private Book book;

    // 댓글 수 증가
    public void increaseCommentCnt() {
        this.commentCnt++;
    }

    // 댓글 수 감소
    public void decreaseCommentCnt() {
        if (this.commentCnt > 0) {
            this.commentCnt--;
        }
    }
}
