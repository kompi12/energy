package com.example.energy.controller;

import com.example.energy.model.Measurement;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.MeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Measurement")
public class MeasurementController {

    @Autowired
    MeasurementService measurementService;

    public EnergyResponse<List<Measurement>> getAllMeasurements() {
        try {
            List<Measurement> listOfPeople = measurementService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
