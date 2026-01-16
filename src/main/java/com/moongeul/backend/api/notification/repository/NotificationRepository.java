package com.moongeul.backend.api.notification.repository;

import com.moongeul.backend.api.notification.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {

}
