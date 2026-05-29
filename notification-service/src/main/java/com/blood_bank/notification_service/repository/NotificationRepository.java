package com.blood_bank.notification_service.repository;

import com.blood_bank.notification_service.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRequestId(Long requestId);
    List<Notification> findByDonorId(Long donorId);
}
