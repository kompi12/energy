package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeterRow {
    private String hepMBR;
    private String meterCode;
    private int diff;

    public MeterRow(String hepMBR, String meterCode, int diff) {
        this.hepMBR = hepMBR;
        this.meterCode = meterCode;
        this.diff = diff;
    }

}