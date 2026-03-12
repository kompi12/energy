package com.example.energy.controller;

import com.example.energy.model.Apartment;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.ApartmentService;
import com.example.energy.viewmodel.ApartmentViewModel;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.RequestBodyPersonViewModel;
import com.example.energy.viewmodel.dto.UpdateApartmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/apartments")
@CrossOrigin(origins = {"http://localhost:5173"})
public class ApartmentController {


    private final ApartmentService service;

    public ApartmentController(ApartmentService service) {
        this.service = service;
    }

    public EnergyResponse<List<Apartment>> getAllapartments() {
        try {
            List<Apartment> listOfPeople = service.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }
    @PostMapping("/personInfo")
    public EnergyResponse<List<ApartmentViewModel>> getInfoAboutApartmentByPersonName(@RequestBody  RequestBodyPersonViewModel viewModel) {
        try {
            List<ApartmentViewModel> listOfPeople = service.getApartmentByPersonName(viewModel.getName());
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @GetMapping("/{id}")
    public DTO.ApartmentDto get(@PathVariable Long id) {
        return service.getApartment(id);
    }

    @PutMapping("/{id}")
    public DTO.ApartmentDto update(@PathVariable Long id, @RequestBody DTO.ApartmentUpdateRequest req) {
        return service.updateApartment(id, req);
    }

    @GetMapping("/{id}/devices")
    public DTO.ApartmentDevicesDto devices(@PathVariable Long id) {
        return service.getDevices(id);
    }


    @PostMapping("/{aptId}/meters")
    public DTO.MeterDto addMeter(@PathVariable Long aptId, @RequestBody DTO.MeterCreateRequest req) {
        return service.addMeterToApartment(aptId, req);
    }

    @PostMapping("/{aptId}/waterMeters")
    public DTO.WaterMeterDto addWaterMeter(@PathVariable Long aptId, @RequestBody DTO.WaterMeterCreateRequest req) {
        return service.addWaterMeterToApartment(aptId, req);
    }

    @PutMapping("/{id}/v2")
    public DTO.ApartmentDto updateV2(@PathVariable Long id, @RequestBody UpdateApartmentRequest req) {
        return service.updateApartment(id, req);
    }

}
