package com.example.energy.viewmodel;

import com.example.energy.model.Building;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class BuildingViewModel {

    private String name;
    private List<BuildingAddressViewModel> addresses;
    private List<ApartmentViewModel> apartments;

    public static BuildingViewModel createViewModel(Building building) {
        BuildingViewModel vm = new BuildingViewModel();
        vm.setName(building.getName());

        if (building.getAddresses() != null) {
            vm.setAddresses(
                    building.getAddresses().stream()
                            .map(BuildingAddressViewModel::create)
                            .collect(Collectors.toList())
            );
        }

        if (building.getApartments() != null) {
            vm.setApartments(
                    building.getApartments().stream()
                            .map(ApartmentViewModel::createViewModel)
                            .collect(Collectors.toList())
            );
        }

        return vm;
    }
}
