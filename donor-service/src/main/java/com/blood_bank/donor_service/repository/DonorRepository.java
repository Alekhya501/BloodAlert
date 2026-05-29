package com.blood_bank.donor_service.repository;

import com.blood_bank.donor_service.model.Donor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    @Query(value = "SELECT * FROM donors d WHERE d.blood_group = :bloodGroup AND ST_Distance_Sphere(d.location, :location) <= :radius", nativeQuery = true)
    List<Donor> findNearbyDonors(@Param("bloodGroup") String bloodGroup, @Param("location") Point location, @Param("radius") double radius);
}
