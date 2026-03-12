package com.example.energy.viewmodel.dto;

import java.util.List;

public class CreateApartmentRequest {
    public String apartmentNumber;
    public Boolean active;
    public Integer priority;
    public Integer sequence;
    public String mbr;
    public String hepMBR;
    public String hepMBRWater;
    public String mjernoMjesto;
    public Double decimalno;

    public PersonIn person; // optional

    public static class PersonIn {
        public Long id; // optional
        public String firstName;
        public String lastName;
        public String contact;
    }
}