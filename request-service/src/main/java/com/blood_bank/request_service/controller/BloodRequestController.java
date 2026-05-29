package com.blood_bank.request_service.controller;

import com.blood_bank.request_service.dto.BloodRequestCreationDTO;
import com.blood_bank.request_service.model.BloodRequest;
import com.blood_bank.request_service.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class BloodRequestController {

    private final RequestService requestService;

    @PostMapping
    public ResponseEntity<BloodRequest> createRequest(@RequestBody BloodRequestCreationDTO dto) {
        return ResponseEntity.ok(requestService.createRequest(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BloodRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @GetMapping
    public List<BloodRequest> getAllRequests() {
        return requestService.getAllRequests();
    }

    @GetMapping("/hospital/{hospitalId}")
    public List<BloodRequest> getRequestsByHospital(@PathVariable Long hospitalId) {
        return requestService.getRequestsByHospital(hospitalId);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BloodRequest> updateRequestStatus(@PathVariable Long id, @RequestParam BloodRequest.RequestStatus status) {
        return ResponseEntity.ok(requestService.updateRequestStatus(id, status));
    }
}
