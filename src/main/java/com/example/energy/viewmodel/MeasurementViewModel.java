package com.example.energy.viewmodel;

import com.example.energy.model.Measurement;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MeasurementViewModel {


    private MeterViewModel meter;

    private Date month;

    private Integer value;

    public static MeasurementViewModel createViewModel(Measurement measurement){
        MeasurementViewModel measurementViewModel = new MeasurementViewModel();
        measurementViewModel.setMeter(measurementViewModel.getMeter());
        measurementViewModel.setMonth(measurementViewModel.getMonth());
        measurementViewModel.setValue(measurementViewModel.getValue());
        return measurementViewModel;
    }

}
