package com.blood_bank.notification_service.consumer;

import com.blood_bank.notification_service.client.DonorClient;
import com.blood_bank.notification_service.dto.DonorResponse;
import com.blood_bank.notification_service.event.BloodRequestCreatedEvent;
import com.blood_bank.notification_service.model.Notification;
import com.blood_bank.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final DonorClient donorClient;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "blood-requests-topic", groupId = "notification-group")
    public void consumeBloodRequest(BloodRequestCreatedEvent event) {
        log.info("Received blood request event: {}", event);

        try {
            // Find nearby donors for the requested blood group
            List<DonorResponse> nearbyDonors = donorClient.searchNearbyDonors(
                    event.getBloodGroup(),
                    event.getLatitude(),
                    event.getLongitude(),
                    10000.0 // 10km radius
            );

            log.info("Found {} nearby donors for blood group {}", nearbyDonors.size(), event.getBloodGroup());

            // Simulate sending alerts and save to DB
            for (DonorResponse donor : nearbyDonors) {
                sendAlert(donor, event);
                saveNotification(donor, event);
            }
        } catch (Exception e) {
            log.error("Error processing blood request notification: {}", e.getMessage(), e);
        }
    }

    private void saveNotification(DonorResponse donor, BloodRequestCreatedEvent request) {
        Notification notification = Notification.builder()
                .requestId(request.getRequestId())
                .donorId(donor.getId())
                .donorName(donor.getName())
                .bloodGroup(donor.getBloodGroup())
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    private void sendAlert(DonorResponse donor, BloodRequestCreatedEvent request) {
        log.info(">>> ALERT: Sending notification to Donor {} ({}) via {} - Request: {} blood urgently needed at Hospital ID {}! Message: {}",
                donor.getName(), donor.getBloodGroup(), donor.getPhoneNumber(), request.getBloodGroup(), request.getHospitalId(), request.getMessage());
        // In a real system, integrate with Twilio, Firebase, or an Email service here.
    }
}
