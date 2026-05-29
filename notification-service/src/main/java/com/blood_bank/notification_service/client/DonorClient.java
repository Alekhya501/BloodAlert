package com.blood_bank.notification_service.client;

import com.blood_bank.notification_service.dto.DonorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "donor-service")
public interface DonorClient {

    @GetMapping("/api/donors/search")
    List<DonorResponse> searchNearbyDonors(
            @RequestParam("bloodGroup") String bloodGroup,
            @RequestParam("latitude") double latitude,
            @RequestParam("longitude") double longitude,
            @RequestParam(value = "radiusInMeters", defaultValue = "10000") double radiusInMeters);
}
