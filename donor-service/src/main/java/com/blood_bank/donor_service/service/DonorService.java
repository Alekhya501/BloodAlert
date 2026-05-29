package com.blood_bank.donor_service.service;

import com.blood_bank.donor_service.dto.DonorRegistrationRequest;
import com.blood_bank.donor_service.dto.DonorResponse;
import com.blood_bank.donor_service.model.Donor;
import com.blood_bank.donor_service.repository.DonorRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public DonorResponse registerDonor(DonorRegistrationRequest request) {
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        
        Donor donor = Donor.builder()
                .name(request.getName())
                .bloodGroup(request.getBloodGroup())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .location(location)
                .available(true)
                .build();

        Donor savedDonor = donorRepository.save(donor);
        return mapToResponse(savedDonor);
    }

    public List<DonorResponse> searchNearbyDonors(String bloodGroup, double latitude, double longitude, double radiusInMeters) {
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return donorRepository.findNearbyDonors(bloodGroup, location, radiusInMeters)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DonorResponse> getAllDonors() {
        return donorRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDonor(Long id) {
        if (!donorRepository.existsById(id)) {
            throw new com.blood_bank.donor_service.exception.ResourceNotFoundException("Donor not found with id: " + id);
        }
        donorRepository.deleteById(id);
    }

    public DonorResponse getDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new com.blood_bank.donor_service.exception.ResourceNotFoundException("Donor not found with id: " + id));
        return mapToResponse(donor);
    }

    @Transactional
    public DonorResponse toggleAvailability(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new com.blood_bank.donor_service.exception.ResourceNotFoundException("Donor not found with id: " + id));
        donor.setAvailable(!donor.isAvailable());
        return mapToResponse(donorRepository.save(donor));
    }

    private DonorResponse mapToResponse(Donor donor) {
        return new DonorResponse(
                donor.getId(),
                donor.getName(),
                donor.getBloodGroup(),
                donor.getEmail(),
                donor.getPhoneNumber(),
                donor.getLocation().getY(),
                donor.getLocation().getX(),
                donor.isAvailable()
        );
    }
}
