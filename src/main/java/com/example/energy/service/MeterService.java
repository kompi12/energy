package com.example.energy.service;

import com.example.energy.model.Meter;
import com.example.energy.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeterService {



    private final MeterRepository meterRepository;

    public MeterService(MeterRepository meterRepository) {
        this.meterRepository = meterRepository;
    }

    public List<Meter> findAll() {
        return meterRepository.findAll();
    }
}
