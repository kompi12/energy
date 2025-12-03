package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeasurementRow {

    private String hepMbr;
    private Integer value;
    private String personName;
    private String address;



    public MeasurementRow(String hepMbr, Integer value) {
        this.hepMbr = hepMbr;
        this.value = value;

    }

    public MeasurementRow(String hepMbr, Integer value,String personName, String address) {
        this.hepMbr = hepMbr;
        this.value = value;
        this.personName = personName;
        this.address = address;
    }
}
