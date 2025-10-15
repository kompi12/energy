package com.example.energy.controller;

import com.example.energy.model.Meter;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.MeterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/meter")
public class MeterController {

    @Autowired
    MeterService meterService;

    public EnergyResponse<List<Meter>> getAllMeter() {
        try {
            List<Meter> listOfPeople = meterService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
