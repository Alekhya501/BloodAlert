package com.blood_bank.request_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodRequestCreatedEvent {
    private Long requestId;
    private Long hospitalId;
    private String bloodGroup;
    private String urgency;
    private double latitude;
    private double longitude;
    private String message;
}
