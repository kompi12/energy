package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExportDataViewModel {

    private String date;
    private List<String> lists;
}
