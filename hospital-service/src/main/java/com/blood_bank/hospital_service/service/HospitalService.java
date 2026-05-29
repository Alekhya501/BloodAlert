package com.blood_bank.hospital_service.service;

import com.blood_bank.hospital_service.dto.HospitalRegistrationRequest;
import com.blood_bank.hospital_service.exception.ResourceNotFoundException;
import com.blood_bank.hospital_service.model.Hospital;
import com.blood_bank.hospital_service.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional
    public Hospital registerHospital(HospitalRegistrationRequest request) {
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        
        Hospital hospital = Hospital.builder()
                .name(request.getName())
                .address(request.getAddress())
                .contactNumber(request.getContactNumber())
                .location(location)
                .build();

        return hospitalRepository.save(hospital);
    }

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
    }

    @Transactional
    public Hospital updateHospital(Long id, HospitalRegistrationRequest request) {
        Hospital hospital = getHospitalById(id);
        
        Point location = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        
        hospital.setName(request.getName());
        hospital.setAddress(request.getAddress());
        hospital.setContactNumber(request.getContactNumber());
        hospital.setLocation(location);

        return hospitalRepository.save(hospital);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    @Transactional
    public void deleteHospital(Long id) {
        if (!hospitalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + id);
        }
        hospitalRepository.deleteById(id);
    }
}
