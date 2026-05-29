package com.blood_bank.hospital_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HospitalRegistrationRequest {
    @NotBlank(message = "Hospital name is required")
    @Size(min = 2, max = 150, message = "Hospital name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Size(min = 8, max = 15, message = "Contact number must be between 8 and 15 characters")
    private String contactNumber;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}
