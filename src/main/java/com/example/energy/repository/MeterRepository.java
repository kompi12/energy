package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Optional<Meter> findByCode(String code);

    @Query("SELECT m FROM Meter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id = :buildingId AND m.code = :id")
    Optional<Meter> findByBuildingAndMeter(@Param("buildingId") Long buildingId, @Param("id") Long id);

    @Query("SELECT m FROM Meter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id IN (:buildingId) AND m.code = :id")
    Optional<Meter> findByBuildingsAndMeter(@Param("buildingId") List<Long> buildingId, @Param("id") Long id);

}