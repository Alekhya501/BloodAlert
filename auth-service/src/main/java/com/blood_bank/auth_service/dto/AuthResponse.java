package com.blood_bank.auth_service.dto;

import com.blood_bank.auth_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String refreshToken;
    private String accesstoken;
    private Role role;
    private int userId;

    public AuthResponse(String accessToken, String refreshToken, String name, int id) {
    }
}
