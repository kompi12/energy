package com.example.energy.viewmodel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExportRow {
    private String hepMBR;
    private String meterCode;
    private int diff;
    private int total;

    public ExportRow(String hepMBR, String meterCode, int diff, int total) {
        this.hepMBR = hepMBR;
        this.meterCode = meterCode;
        this.diff = diff;
        this.total = total;
    }

}