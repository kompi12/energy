package com.example.energy.viewmodel;

import com.example.energy.model.Apartment;
import com.example.energy.model.Meter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class MeterViewModel {

    private String code;

    private ApartmentViewModel apartment;

    private List<MeasurementViewModel> measurements;

    public static  MeterViewModel createViewModel(Meter meter) {
        MeterViewModel meterViewModel = new MeterViewModel();
        meterViewModel.setCode(meter.getCode());
        meterViewModel.setApartment(ApartmentViewModel.createViewModel(meter.getApartment()));
        meterViewModel.setMeasurements(meter.getMeasurements().stream().map(MeasurementViewModel::createViewModel).collect(Collectors.toList()));
        return meterViewModel;
    }

    }
