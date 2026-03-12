package com.example.energy.viewmodel;

import com.example.energy.model.Apartment;
import com.example.energy.model.Person;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ApartmentViewModel {


    private String apartmentNumber;

    private BuildingViewModel building;

    private PersonViewModel person;

    private String personFirstName;

    private Long apartmentId;

    private String mbr;

    private String hepMBR;

    private List<MeterViewModel> meters;





    public static ApartmentViewModel createViewModel(Apartment apartment){
        ApartmentViewModel vm = new ApartmentViewModel();
        vm.setApartmentNumber(apartment.getApartmentNumber());
        vm.setApartmentId(apartment.getId());
        vm.setMbr(apartment.getMbr());
        vm.setHepMBR(apartment.getHepMBR());
        vm.setPersonFirstName(apartment.getPerson().getFirstName());
        return vm;
    }


}
