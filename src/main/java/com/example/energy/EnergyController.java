package com.example.energy.controller;

import com.example.energy.repository.EnergyRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyRepository energyRepository;

    public EnergyController(EnergyRepository energyRepository) {
        this.energyRepository = energyRepository;
    }

    @GetMapping("/test")
    public String testDb() {
        long count = energyRepository.count();
        return "Connected to DB! Found " + count + " records.";
    }
}
