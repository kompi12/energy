package com.example.energy.viewmodel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MeterMeasurementExcelRow {

    private String apartmentName;

    private String meterSerial;

    private Double value;
    private LocalDate date;
}
