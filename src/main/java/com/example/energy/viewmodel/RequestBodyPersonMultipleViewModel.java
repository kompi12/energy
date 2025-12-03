package com.example.energy.viewmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestBodyPersonMultipleViewModel {

    private String name;
    private List<String> code;
    private String apartmentId;
}
