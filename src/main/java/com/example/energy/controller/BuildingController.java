package com.example.energy.controller;

import com.example.energy.model.Building;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/building")
public class BuildingController {

    @Autowired
    BuildingService buildingService;

    public EnergyResponse<List<Building>> getAllBuildings() {
        try {
            List<Building> listOfPeople = buildingService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
