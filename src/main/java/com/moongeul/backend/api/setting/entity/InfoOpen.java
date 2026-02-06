package com.moongeul.backend.api.setting.entity;

import com.moongeul.backend.api.member.entity.Member;
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
@Table(name = "INFO_OPEN")
public class InfoOpen extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false)
    private Boolean isPublic; // 전체 공개

    @Column(nullable = false)
    private Boolean isFollowersOnly; // 팔로우한테만 공개

    @Column(nullable = false)
    private Boolean isPrivate; // 비공개

    public void update(boolean isPublic, boolean isFollowersOnly, boolean isPrivate) {
        this.isPublic = isPublic;
        this.isFollowersOnly = isFollowersOnly;
        this.isPrivate = isPrivate;
    }

    public static InfoOpen createDefault(Member member) {
        return InfoOpen.builder()
                .member(member)
                .isPublic(true)
                .isFollowersOnly(false)
                .isPrivate(false)
                .build();
    }
}
