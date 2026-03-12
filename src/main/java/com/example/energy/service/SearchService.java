package com.example.energy.service;

import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.repository.WaterMeterRepository;
import com.example.energy.viewmodel.dto.ApartmentSearchResultDto;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.DeviceSearchResultDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final ApartmentRepository apartmentRepo;
    private final WaterMeterRepository waterRepo;
    private final MeterRepository meterRepo;

    public SearchService(ApartmentRepository apartmentRepo,
                         WaterMeterRepository waterRepo,
                         MeterRepository meterRepo) {
        this.apartmentRepo = apartmentRepo;
        this.waterRepo = waterRepo;
        this.meterRepo = meterRepo;
    }

    public List<ApartmentSearchResultDto> apartments(String q) {
        String qq = q == null ? "" : q.trim();
        if (qq.length() < 2) return List.of();
        return apartmentRepo.searchApartments(qq);
    }

    public List<DeviceSearchResultDto> waterMeters(String q) {
        String qq = q == null ? "" : q.trim();
        if (qq.length() < 2) return List.of();
        return waterRepo.searchWaterMetersByCode(qq);
    }

    public List<DeviceSearchResultDto> meters(String q) {
        String qq = q == null ? "" : q.trim();
        if (qq.length() < 2) return List.of();
        return meterRepo.searchMetersByCode(qq);
    }
}
