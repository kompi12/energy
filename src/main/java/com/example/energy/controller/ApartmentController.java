package com.example.energy.controller;

import com.example.energy.model.Apartment;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/apartment")
public class ApartmentController {

    @Autowired
    ApartmentService apartmentService;

    public EnergyResponse<List<Apartment>> getAllapartments() {
        try {
            List<Apartment> listOfPeople = apartmentService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
