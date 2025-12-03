package com.example.energy.viewmodel;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeasurementRowWithPerson {
    private String hepMbr;
    private int value;
    private String personName;

    public MeasurementRowWithPerson(String hepMbr, int value, String personName) {
        this.hepMbr = hepMbr;
        this.value = value;
        this.personName = personName;
    }


}
