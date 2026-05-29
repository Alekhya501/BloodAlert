package com.blood_bank.notification_service.controller;

import com.blood_bank.notification_service.model.Notification;
import com.blood_bank.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    @GetMapping("/request/{requestId}")
    public List<Notification> getNotificationsByRequest(@PathVariable Long requestId) {
        return notificationRepository.findByRequestId(requestId);
    }

    @GetMapping("/donor/{donorId}")
    public List<Notification> getNotificationsByDonor(@PathVariable Long donorId) {
        return notificationRepository.findByDonorId(donorId);
    }
}
