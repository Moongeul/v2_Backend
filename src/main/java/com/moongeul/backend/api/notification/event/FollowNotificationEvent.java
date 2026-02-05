package com.moongeul.backend.api.notification.event;

import com.moongeul.backend.api.member.entity.Member;

public record FollowNotificationEvent(
        
    Member receiver, // 알림을 받을 사람
    Member actor    // 팔로우를 건 사람    
) {}
