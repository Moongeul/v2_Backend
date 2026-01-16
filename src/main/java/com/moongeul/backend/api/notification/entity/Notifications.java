package com.moongeul.backend.api.notification.entity;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notifications")
public class Notifications extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver; // 알림을 받는 사람

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private Member actor;    // 알림 발생시킨 사람

    private String content; // 알림 내용

    @Enumerated(EnumType.STRING)
    private NotificationType type; // NOTICE, LIKE, COMMENT 등

    private Long relatedId; // 클릭 시 이동할 게시글 ID or 유저 ID or 공지사항 ID

    private boolean isRead; // 읽음 처리 여부
}
