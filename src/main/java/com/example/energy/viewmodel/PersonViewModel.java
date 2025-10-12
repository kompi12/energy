package com.example.energy.viewmodel;

import com.example.energy.model.Apartment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PersonViewModel {


    private String firstName;

    private String lastName;

    private String contact;

    private List<ApartmentViewModel> apartments;

}
