package com.blood_bank.donor_service.controller;

import com.blood_bank.donor_service.dto.DonorRegistrationRequest;
import com.blood_bank.donor_service.dto.DonorResponse;
import com.blood_bank.donor_service.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @PostMapping("/register")
    public ResponseEntity<DonorResponse> registerDonor(@Valid @RequestBody DonorRegistrationRequest request) {
        return ResponseEntity.ok(donorService.registerDonor(request));
    }

    @GetMapping
    public List<DonorResponse> getAllDonors() {
        return donorService.getAllDonors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonorResponse> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonor(@PathVariable Long id) {
        donorService.deleteDonor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<DonorResponse> toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.toggleAvailability(id));
    }

    @GetMapping("/search")
    public List<DonorResponse> searchNearbyDonors(
            @RequestParam String bloodGroup,
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "10000") double radiusInMeters) {
        
        return donorService.searchNearbyDonors(bloodGroup, latitude, longitude, radiusInMeters);
    }
}
