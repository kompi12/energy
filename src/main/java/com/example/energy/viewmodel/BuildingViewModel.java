package com.example.energy.viewmodel;

import com.example.energy.model.Apartment;
import com.example.energy.model.Building;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class BuildingViewModel {


    private String address;

    private String name;

    private List<ApartmentViewModel> apartments;

    public static BuildingViewModel createViewModel(Building building) {
            BuildingViewModel buildingViewModel = new BuildingViewModel();
            buildingViewModel.setName(building.getName());
            buildingViewModel.setAddress(building.getAddress());
        buildingViewModel.setApartments(
                building.getApartments()
                        .stream()
                        .map(ApartmentViewModel::createViewModel)
                        .collect(Collectors.toList())
        );
            return buildingViewModel;
    }

  }
