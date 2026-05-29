package com.blood_bank.notification_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long requestId;
    private Long donorId;
    private String donorName;
    private String bloodGroup;
    private String message;
    private LocalDateTime sentAt;
}
