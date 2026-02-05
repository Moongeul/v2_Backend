package com.moongeul.backend.api.notification.event;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.question.entity.Question;

public record AnswerNotificationEvent(

    Member receiver,    // 알림을 받을 사람 (질문 작성자)
    Member actor,       // 댓글을 작성한 사람
    Question question   // 어떤 질문인지
) {}
