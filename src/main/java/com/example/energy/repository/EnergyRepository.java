package com.example.energy.repository;

import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnergyRepository extends JpaRepository<Energy, Long> {
}
