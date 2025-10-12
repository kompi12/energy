package com.example.energy.repository;

import com.example.energy.model.Energy;
import com.example.energy.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
}
