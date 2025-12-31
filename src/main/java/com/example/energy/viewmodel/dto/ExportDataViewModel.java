package com.example.energy.viewmodel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExportDataViewModel {

    private String date;
    private Integer year;
    private Integer month;
    private List<String> lists;
}
