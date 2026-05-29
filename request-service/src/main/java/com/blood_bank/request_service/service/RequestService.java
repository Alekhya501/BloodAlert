package com.blood_bank.request_service.service;

import com.blood_bank.request_service.dto.BloodRequestCreationDTO;
import com.blood_bank.request_service.event.BloodRequestCreatedEvent;
import com.blood_bank.request_service.exception.ResourceNotFoundException;
import com.blood_bank.request_service.model.BloodRequest;
import com.blood_bank.request_service.repository.BloodRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final KafkaTemplate<String, BloodRequestCreatedEvent> kafkaTemplate;

    private static final String TOPIC = "blood-requests-topic";

    @Transactional
    public BloodRequest createRequest(BloodRequestCreationDTO dto) {
        BloodRequest request = BloodRequest.builder()
                .hospitalId(dto.getHospitalId())
                .bloodGroup(dto.getBloodGroup())
                .urgency(dto.getUrgency())
                .message(dto.getMessage())
                .createdAt(LocalDateTime.now())
                .status(BloodRequest.RequestStatus.OPEN)
                .build();

        BloodRequest savedRequest = bloodRequestRepository.save(request);

        BloodRequestCreatedEvent event = BloodRequestCreatedEvent.builder()
                .requestId(savedRequest.getId())
                .hospitalId(savedRequest.getHospitalId())
                .bloodGroup(savedRequest.getBloodGroup())
                .urgency(savedRequest.getUrgency())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .message(savedRequest.getMessage())
                .build();

        log.info("Publishing blood request event to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, String.valueOf(event.getRequestId()), event);

        return savedRequest;
    }

    @Transactional
    public BloodRequest updateRequestStatus(Long id, BloodRequest.RequestStatus status) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));
        request.setStatus(status);
        return bloodRequestRepository.save(request);
    }

    public List<BloodRequest> getRequestsByHospital(Long hospitalId) {
        return bloodRequestRepository.findByHospitalId(hospitalId);
    }

    public List<BloodRequest> getAllRequests() {
        return bloodRequestRepository.findAll();
    }

    public BloodRequest getRequestById(Long id) {
        return bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));
    }
}
