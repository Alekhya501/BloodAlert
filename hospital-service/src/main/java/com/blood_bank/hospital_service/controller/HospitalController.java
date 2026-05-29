package com.blood_bank.hospital_service.controller;

import com.blood_bank.hospital_service.dto.HospitalRegistrationRequest;
import com.blood_bank.hospital_service.model.Hospital;
import com.blood_bank.hospital_service.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping("/register")
    public ResponseEntity<Hospital> registerHospital(@Valid @RequestBody HospitalRegistrationRequest request) {
        return ResponseEntity.ok(hospitalService.registerHospital(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getHospitalById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getHospitalById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Hospital> updateHospital(@PathVariable Long id, @Valid @RequestBody HospitalRegistrationRequest request) {
        return ResponseEntity.ok(hospitalService.updateHospital(id, request));
    }

    @GetMapping
    public List<Hospital> getAllHospitals() {
        return hospitalService.getAllHospitals();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHospital(@PathVariable Long id) {
        hospitalService.deleteHospital(id);
        return ResponseEntity.noContent().build();
    }
}
