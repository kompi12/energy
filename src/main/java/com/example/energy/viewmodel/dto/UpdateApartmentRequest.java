package com.example.energy.viewmodel.dto;

public class UpdateApartmentRequest {
    public String apartmentNumber;
    public Boolean active;
    public Integer priority;
    public Integer sequence;
    public String mbr;
    public String hepMBR;
    public String hepMBRWater;
    public String mjernoMjesto;
    public Double decimalno;

    // Person:
    // - ako je personId null i clearPerson=true -> makni osobu s apartmana
    // - ako je personId != null -> link na postojeću osobu
    // - ako person data ima firstName -> kreiraj novu osobu i linkaj
    public Long personId;
    public Boolean clearPerson;

    public PersonIn person; // optional create-new
    public static class PersonIn {
        public String firstName;
        public String lastName;
        public String contact;
    }
}
