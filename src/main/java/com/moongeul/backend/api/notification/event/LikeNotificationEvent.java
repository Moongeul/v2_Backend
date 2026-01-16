package com.moongeul.backend.api.notification.event;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.post.entity.Post;

public record LikeNotificationEvent(

    Member receiver, // 알림을 받을 사람 (게시글 작성자)
    Member actor,    // 공감을 누른 사람
    Post post        // 어떤 게시글인지
) {}
