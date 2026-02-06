package com.example.energy.repository;

import com.example.energy.model.Energy;
import com.example.energy.model.Measurement;
import com.example.energy.model.Meter;
import com.example.energy.model.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    Optional<Measurement> findByMeterAndMeasureDate(Meter meter, LocalDate measureDate);

    List<Measurement> findAllByMeterIn(Collection<Meter> meters);

    List<Measurement> findAllByWaterMeterIn(Collection<WaterMeter> meters);


}
