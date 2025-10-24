package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Optional<Building> findByCodeIgnoreCaseAndCity_NameIgnoreCase(String code, String cityName);
    Optional<Building> findByCodeIgnoreCase(String code);
}