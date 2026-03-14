package com.moongeul.backend.api.notification.service;

import com.moongeul.backend.api.member.entity.Member;
import com.moongeul.backend.api.member.repository.MemberRepository;
import com.moongeul.backend.api.notification.dto.NotificationsResponseDTO;
import com.moongeul.backend.api.notification.dto.checkNotificationsResponseDTO;
import com.moongeul.backend.api.notification.entity.Notifications;
import com.moongeul.backend.api.notification.repository.NotificationRepository;
import com.moongeul.backend.common.exception.NotFoundException;
import com.moongeul.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    /* 알림 내역 전체 조회 */
    @Transactional
    public NotificationsResponseDTO getNotifications(Integer page, Integer size, String email) {
        Member member = getMemberByEmail(email);

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Notifications> notificationsPage = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(member.getId(), pageable);

        List<NotificationsResponseDTO.NotificationInfo> infoList = notificationsPage.getContent().stream()
                .map(notification -> NotificationsResponseDTO.NotificationInfo.builder()
                        .id(notification.getId())
                        .relatedId(notification.getRelatedId())
                        .notificationType(notification.getType())
                        .profileImage(notification.getActor() != null ? notification.getActor().getProfileImage() : null)
                        .content(notification.getContent())
                        .createdAt(notification.getCreatedAt())
                        .isRead(notification.isRead())
                        .build())
                .collect(Collectors.toList());

        notificationRepository.updateIsReadByReceiverId(member.getId());

        return NotificationsResponseDTO.builder()
                .total(notificationsPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(notificationsPage.getTotalPages())
                .isLast(notificationsPage.isLast())
                .data(infoList)
                .build();
    }

    @Transactional
    public checkNotificationsResponseDTO checkUnReadNotifications(String email){
        Member member = getMemberByEmail(email);

        boolean isExist = notificationRepository.existsByReceiverIdAndIsReadFalse(member.getId());
        Long count = notificationRepository.countByReceiverIdAndIsReadFalse(member.getId());

        return checkNotificationsResponseDTO.builder()
                .exist(isExist)
                .count(count)
                .build();
    }


    /*
     * 단순 데이터 불러오기용 코드 메서드 - 코드 깔끔하게 하기용
     */

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOTFOUND_EXCEPTION.getMessage()));
    }
}
