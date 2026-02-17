package com.moongeul.backend.api.notification.repository;

import com.moongeul.backend.api.notification.entity.Notifications;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    Slice<Notifications> findByReceiverIdOrderByCreatedAtDesc(Long id, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notifications n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    void updateIsReadByReceiverId(@Param("receiverId") Long receiverId);

    // 읽지 않은 알림이 있는지 여부 확인 (EXISTS 쿼리 사용으로 빠름)
    boolean existsByReceiverIdAndReadFalse(Long receiverId);

    // 읽지 않은 알림의 개수 확인
    long countByReceiverIdAndReadFalse(Long receiverId);

}
