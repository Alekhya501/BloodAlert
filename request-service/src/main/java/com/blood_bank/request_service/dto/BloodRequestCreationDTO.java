package com.blood_bank.request_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BloodRequestCreationDTO {
    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    @NotBlank(message = "Blood group is required")
    private String bloodGroup;

    @NotBlank(message = "Urgency level is required")
    private String urgency;

    private String message;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}
