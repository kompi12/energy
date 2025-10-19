package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Meter findByCode(String code);
}
