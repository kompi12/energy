package com.example.energy.controller;

import com.example.energy.model.Building;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.BuildingService;
import com.example.energy.viewmodel.dto.CreateApartmentRequest;
import com.example.energy.viewmodel.dto.CreateBuildingRequest;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.mapper.DTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@CrossOrigin(origins = {"http://localhost:5173"})
public class BuildingController {

    private final BuildingService service;

    public BuildingController(BuildingService service) {
        this.service = service;
    }

    public EnergyResponse<List<Building>> getAllBuildings() {
        try {
            List<Building> listOfPeople = service.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @GetMapping
    public List<DTO.BuildingDto> list() {
        return service.getAllBuildings();
    }

    @GetMapping("/{id}")
    public DTO.BuildingDto get(@PathVariable Long id) {
        return service.getBuilding(id);
    }

    @GetMapping("/{id}/summary")
    public DTO.BuildingSummaryDto summary(@PathVariable Long id) {
        return service.getSummary(id);
    }

    @GetMapping("/{id}/apartments")
    public List<DTO.ApartmentRowDto> apartments(@PathVariable Long id) {
        return service.listApartmentsForBuilding(id);
    }

    @PostMapping
    public DTO.BuildingDto create(@RequestBody CreateBuildingRequest req) {
        return service.createFull(req);
    }

    @PostMapping("/listFilterBuildings")
    public List<DTO.BuildingDto> getBuildingsHeat(@RequestBody DTO.RequestBodyHeat req) {
        return service.getAllBuildingsHeater(req);
    }


    @PostMapping("/{id}/apartments")
    public DTO.ApartmentDto createApartment(@PathVariable Long id, @RequestBody CreateApartmentRequest req) {
        return service.createApartment(id, req);
    }
}
