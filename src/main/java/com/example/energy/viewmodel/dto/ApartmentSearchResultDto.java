package com.example.energy.viewmodel.dto;

public record ApartmentSearchResultDto(
        Long apartmentId,
        String apartmentNumber,
        String buildingCode,
        String buildingName,
        String cityName,
        String hepMBR,
        String hepMBRWater,
        String mbr,
        String mjernoMjesto,
        String personFullName,
        String personContact
) {}
