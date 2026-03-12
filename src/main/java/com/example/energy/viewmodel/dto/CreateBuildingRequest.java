package com.example.energy.viewmodel.dto;

import com.example.energy.viewmodel.dto.mapper.DTOMapper;

import java.util.List;

public class CreateBuildingRequest {
    public String code;
    public String name;
    public String techem;
    public Long cityId;

    public List<AddressIn> addresses;
    public List<ApartmentIn> apartments;

    public static class AddressIn {
        public String addressLine;
        public String postalCode;
        public String city;
        public String country; // npr "HR"
    }

    public static class ApartmentIn {
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
    }

    public static class PersonIn {
        public Long id; // optional: link na postojeću osobu
        public String firstName;
        public String lastName;
        public String contact;
    }
}
