package com.example.energy.viewmodel.dto;


import com.example.energy.model.MonthValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.YearMonth;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MeasurementRowWithPersonDynamic {

    private String hepMbr;
    private String personName;
    private String grad;
    private String adresa;
    private String meterCode;

    // Dynamic months
    private Map<YearMonth, MonthValue> monthlyValues;}



