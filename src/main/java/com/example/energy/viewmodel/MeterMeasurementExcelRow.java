package com.example.energy.viewmodel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class MeterMeasurementExcelRow {

    private String apartmentName;

    private String meterSerial;

    private Integer value;
    private LocalDate date;
}
