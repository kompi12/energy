package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeasurementRowWithPersonForMeter {
    private String hepMbr;
    private int value;
    private String mbr;
    private String meterCode;
    private String personName;

    public MeasurementRowWithPersonForMeter(String hepMbr, int value,String code, String personName,String mbr) {
        this.hepMbr = hepMbr;
        this.value = value;
        this.mbr = mbr;
        this.personName = personName;
        this.meterCode = code;
    }

}
