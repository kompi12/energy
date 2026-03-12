package com.example.energy.viewmodel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WaterMeterPersonViewModel {

    private List<String> active;

    private List<String> deactivated;
}
