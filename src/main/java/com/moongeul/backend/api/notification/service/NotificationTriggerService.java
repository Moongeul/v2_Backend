package com.moongeul.backend.api.notification.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.notification.event.AnswerNotificationEvent;
import com.moongeul.backend.api.notification.event.FollowNotificationEvent;
import com.moongeul.backend.api.notification.event.LikeNotificationEvent;
import com.moongeul.backend.api.post.entity.Post;
import com.moongeul.backend.api.question.entity.Question;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationTriggerService {

    private final ApplicationEventPublisher eventPublisher; // Spring Event 발행 객체

    /* 공감 알림 */
    public void likeNotification(Member receiver, Member actor, Post post){
        if (!receiver.getId().equals(actor.getId())) { // 자신의 게시글일 경우 알림 발생 x
            eventPublisher.publishEvent(new LikeNotificationEvent(receiver, actor, post));
        }
    }

    /* 댓글 알림 */
    public void answerNotification(Member receiver, Member actor, Question question){
        eventPublisher.publishEvent(new AnswerNotificationEvent(receiver, actor, question));
    }

    /* 팔로우 알림 */
    public void followNotification(Member receiver, Member actor){
        eventPublisher.publishEvent(new FollowNotificationEvent(receiver, actor));
    }
}
