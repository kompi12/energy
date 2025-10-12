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

    private List<MeterViewModel> meters;


    public static ApartmentViewModel createViewModel(Apartment apartment){
        ApartmentViewModel apartmentViewModel = new ApartmentViewModel();
        apartmentViewModel.setApartmentNumber(apartment.getApartmentNumber());
        apartmentViewModel.setBuilding(BuildingViewModel.createViewModel(apartment.getBuilding()));
        apartmentViewModel.setMeters(apartment.getMeters().stream().map(MeterViewModel::createViewModel).collect(Collectors.toList()));
        return apartmentViewModel;
    }


}
