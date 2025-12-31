package com.example.energy.controller;

import com.example.energy.model.Apartment;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.ApartmentService;
import com.example.energy.viewmodel.ApartmentViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/personInfo")
    public EnergyResponse<List<ApartmentViewModel>> getInfoAboutApartmentByPersonName(@RequestBody  RequestBodyPersonViewModel viewModel) {
        try {
            List<ApartmentViewModel> listOfPeople = apartmentService.getApartmentByPersonName(viewModel.getName());
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
