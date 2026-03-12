package com.example.energy.service;

import com.example.energy.model.Measurement;
import com.example.energy.model.Meter;
import com.example.energy.model.WaterMeter;
import com.example.energy.repository.MeasurementRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.repository.WaterMeterRepository;
import com.example.energy.service.audit.AuditService;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.mapper.DTOMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepo;
    private final MeterRepository meterRepo;
    private final WaterMeterRepository waterMeterRepo;
    private final AuditService  auditService;

    public MeasurementService(MeasurementRepository measurementRepo,
                              MeterRepository meterRepo,
                              WaterMeterRepository waterMeterRepo,AuditService auditService) {
        this.measurementRepo = measurementRepo;
        this.meterRepo = meterRepo;
        this.waterMeterRepo = waterMeterRepo;
        this.auditService = auditService;
    }


    public List<Measurement> findAll() {
        return measurementRepo.findAll();
    }


    @Transactional(readOnly = true)
    public List<DTO.MeasurementDto> listForMeter(Long meterId) {
        return measurementRepo.findByMeter_IdOrderByMeasureDateDesc(meterId).stream().map(DTOMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DTO.MeasurementDto> listForWaterMeter(Long waterMeterId) {
        return measurementRepo.findByWaterMeter_IdOrderByMeasureDateDesc(waterMeterId).stream().map(DTOMapper::toDto).toList();
    }

    @Transactional
    public DTO.MeasurementDto addForMeter(Long meterId, DTO.MeasurementCreateRequest req) {
        if (req.measureDate() == null) throw new IllegalArgumentException("measureDate is required");
        if (measurementRepo.existsByMeter_IdAndMeasureDate(meterId, req.measureDate())) {
            throw new IllegalArgumentException("Measurement for that date already exists");
        }
        Meter meter = meterRepo.findById(meterId).orElseThrow(() -> new EntityNotFoundException("Meter not found"));

        Measurement m = new Measurement();
        m.setMeter(meter);
        m.setMeasureDate(req.measureDate());
        m.setValue(req.value());
        // createdBy možeš puniti iz security contexta kasnije
        m.setCreatedBy("local");
        measurementRepo.save(m);
        auditService.logEntityChange(
                "ENTITY_CREATE",
                "Measurement",
                String.valueOf(m.getId()),
                null,
                java.util.Map.of(
                        "meterId", meterId,
                        "measureDate", m.getMeasureDate(),
                        "value", m.getValue()
                )
        );
        return DTOMapper.toDto(m);
    }

    @Transactional
    public DTO.MeasurementDto addForWaterMeter(Long waterMeterId, DTO.MeasurementCreateRequest req) {
        if (req.measureDate() == null) throw new IllegalArgumentException("measureDate is required");
        if (measurementRepo.existsByWaterMeter_IdAndMeasureDate(waterMeterId, req.measureDate())) {
            throw new IllegalArgumentException("Measurement for that date already exists");
        }
        WaterMeter wm = waterMeterRepo.findById(waterMeterId).orElseThrow(() -> new EntityNotFoundException("WaterMeter not found"));

        Measurement m = new Measurement();
        m.setWaterMeter(wm);
        m.setMeasureDate(req.measureDate());
        m.setValue(req.value());
        m.setCreatedBy("local");
        measurementRepo.save(m);
        auditService.logEntityChange(
                "ENTITY_CREATE",
                "Measurement",
                String.valueOf(m.getId()),
                null,
                java.util.Map.of(
                        "waterMeterId", waterMeterId,
                        "measureDate", m.getMeasureDate(),
                        "value", m.getValue()
                )
        );
        return DTOMapper.toDto(m);
    }
}
