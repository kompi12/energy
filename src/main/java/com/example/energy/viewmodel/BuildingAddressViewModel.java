package com.example.energy.viewmodel;

import com.example.energy.model.BuildingAddress;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BuildingAddressViewModel {
    private String addressLine;
    private String postalCode;
    private String city;
    private String country;

    public static BuildingAddressViewModel create(BuildingAddress address) {
        BuildingAddressViewModel vm = new BuildingAddressViewModel();
        vm.setAddressLine(address.getAddressLine());
        vm.setPostalCode(address.getPostalCode());
        vm.setCity(address.getCity());
        vm.setCountry(address.getCountry());
        return vm;
    }
}
