package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.BuildingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuildingAddressRepository extends JpaRepository<BuildingAddress, Long> {
    Optional<BuildingAddress> findByBuilding_IdAndAddressLineIgnoreCase(Long buildingId, String addressLine);
    Optional<BuildingAddress> findByAddressLineIgnoreCase(String addressLine); // optional generic lookup
}