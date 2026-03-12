package com.example.energy.viewmodel.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestBodyPersonMultipleViewModel {

    private String name;
    private List<String> code;
    private String apartmentId;
    private String date;
}
