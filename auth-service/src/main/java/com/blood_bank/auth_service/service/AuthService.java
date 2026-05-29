package com.blood_bank.auth_service.service;


import com.blood_bank.auth_service.dto.AuthResponse;
import com.blood_bank.auth_service.dto.RegisterRequest;
import com.blood_bank.auth_service.entity.User;
import com.blood_bank.auth_service.exception.UserAlreadyExistException;
import com.blood_bank.auth_service.exception.UserNotFoundException;
import com.blood_bank.auth_service.repository.UserRepository;
import com.blood_bank.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private JwtUtil util;

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {

        // check if this phone is already registered
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistException(
                    "Phone " + request.getPhone() + " is already registered.");
        }

        // create user and save to MySQL
        User user = new User();
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setRole(request.getRole());
        userRepository.save(user);

        // generate OTP and print in console
        otpService.generateAndSave(request.getPhone());
    }

    public void sendOtp(String phone) {

        // phone must be registered before OTP can be sent
        userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException(
                        "Phone " + phone + " is not registered."));

        // generate new OTP and print in console
        otpService.generateAndSave(phone);
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {

        // validate OTP — throws exception if wrong or expired
        otpService.validate(request.getPhone(), request.getOtp());

        // fetch user after OTP is confirmed
        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found."));

        // generate JWT tokens
        String accessToken = util.generateAccessToken(
                (long) user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken((long) user.getId());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getRole().name(),
                user.getId()
        );
    }
}