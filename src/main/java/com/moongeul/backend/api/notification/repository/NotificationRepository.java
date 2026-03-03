package com.moongeul.backend.api.notification.repository;

import com.moongeul.backend.api.notification.entity.NotificationType;
import com.moongeul.backend.api.notification.entity.Notifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    Page<Notifications> findByReceiverIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void updateIsReadByReceiverId(@Param("receiverId") Long receiverId);

    // 읽지 않은 알림이 있는지 여부 확인 (EXISTS 쿼리 사용으로 빠름)
    boolean existsByReceiverIdAndIsReadFalse(Long receiverId);

    // 읽지 않은 알림의 개수 확인
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // receiverId와 actorId로 FOLLOW_PRIVATE 알림 가져오기
    Optional<Notifications> findByReceiverIdAndActorIdAndType(Long receiverId, Long actorId, NotificationType notificationType);

}
