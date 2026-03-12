package com.example.energy.viewmodel.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MissingMetersDataViewModel {

    private String buildingId;
    private List<String> hepMbr;

}
