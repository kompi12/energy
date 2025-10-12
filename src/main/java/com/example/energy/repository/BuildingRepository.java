package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuildingRepository extends JpaRepository<Building, Long> {
}
