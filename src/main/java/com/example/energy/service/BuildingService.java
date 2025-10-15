package com.example.energy.service;

import com.example.energy.model.Building;
import com.example.energy.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {
    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }
    public List<Building> findAll() {
        return buildingRepository.findAll();
    }
}
