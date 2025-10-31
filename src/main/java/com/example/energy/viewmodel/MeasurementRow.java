package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeasurementRow {

    private String hepMbr;
    private Integer value;

    public MeasurementRow(String hepMbr, Integer value) {
        this.hepMbr = hepMbr;
        this.value = value;
    }
}
