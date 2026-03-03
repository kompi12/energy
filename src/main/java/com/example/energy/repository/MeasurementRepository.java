package com.example.energy.repository;

import com.example.energy.model.Energy;
import com.example.energy.model.Measurement;
import com.example.energy.model.Meter;
import com.example.energy.model.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    Optional<Measurement> findByMeterAndMeasureDate(Meter meter, LocalDate measureDate);

    List<Measurement> findAllByMeterIn(Collection<Meter> meters);

    List<Measurement> findAllByWaterMeterIn(Collection<WaterMeter> meters);

    // Razdjelnici (Meter)
    List<Measurement> findByMeter_IdOrderByMeasureDateDesc(Long meterId);

    // Vodomjeri (WaterMeter)
    List<Measurement> findByWaterMeter_IdOrderByMeasureDateDesc(Long waterMeterId);

    boolean existsByMeter_IdAndMeasureDate(Long meterId, LocalDate measureDate);
    boolean existsByWaterMeter_IdAndMeasureDate(Long waterMeterId, LocalDate measureDate);

    List<Measurement> findByMeter_IdAndMeasureDateBetweenOrderByMeasureDateAsc(
            Long meterId, LocalDate from, LocalDate to
    );

    Optional<Measurement> findTopByMeter_IdAndMeasureDateLessThanOrderByMeasureDateDesc(
            Long meterId, LocalDate before
    );

    // sva očitanja u periodu za sve metre
    @Query("""
        select m
        from Measurement m
        where m.meter.id in :meterIds
          and m.measureDate between :from and :to
        order by m.meter.id asc, m.measureDate asc
    """)
    List<Measurement> findAllForMetersInPeriod(
            @Param("meterIds") List<Long> meterIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // zadnje očitanje prije perioda (1 red po metru)
    @Query("""
        select m
        from Measurement m
        where m.meter.id in :meterIds
          and m.measureDate < :before
          and m.measureDate = (
              select max(m2.measureDate)
              from Measurement m2
              where m2.meter.id = m.meter.id
                and m2.measureDate < :before
          )
    """)
    List<Measurement> findLastBeforeForMeters(
            @Param("meterIds") List<Long> meterIds,
            @Param("before") LocalDate before
    );


        @Modifying
        @Query(value = """
        INSERT INTO measurement (meter_id, measure_date, value)
        VALUES (:meterId, :measureDate, :value)
        ON CONFLICT (meter_id, measure_date) DO NOTHING
        """, nativeQuery = true)
        int insertIgnore(@Param("meterId") Long meterId,
                         @Param("measureDate") LocalDate measureDate,
                         @Param("value") Double value);
    }

