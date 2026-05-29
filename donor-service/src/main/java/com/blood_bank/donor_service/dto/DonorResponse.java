package com.blood_bank.donor_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponse {
    private Long id;
    private String name;
    private String bloodGroup;
    private String email;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    private boolean available;
}
