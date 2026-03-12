package com.example.energy.viewmodel.dto.mapper;

import com.example.energy.model.*;
import com.example.energy.viewmodel.dto.DTO;

import java.util.List;

public class DTOMapper {

    private DTOMapper() {}

    /* -------- Building -------- */

    public static DTO.BuildingDto toDto(Building b) {
        List<DTO.BuildingDto.AddressDto> addresses = b.getAddresses() == null
                ? List.of()
                : b.getAddresses().stream()
                .map(a -> new DTO.BuildingDto.AddressDto(
                        a.getAddressLine(),
                        a.getPostalCode(),
                        a.getCity(),
                        a.getCountry()
                ))
                .toList();

        return new DTO.BuildingDto(
                b.getId(),
                b.getCode(),
                b.getName(),
                b.getTechem(),
                b.getCity() != null ? b.getCity().getName() : null,
                addresses
        );
    }

    /* -------- Person -------- */

    public static DTO.PersonDto toDto(Person p) {
        if (p == null) return null;
        return new DTO.PersonDto(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getContact()
        );
    }

    /* -------- Apartment -------- */

    public static DTO.ApartmentDto toDto(Apartment a) {
        return new DTO.ApartmentDto(
                a.getId(),
                a.getApartmentNumber(),
                Boolean.TRUE.equals(a.getActive()),
                a.getPriority(),
                a.getSequence(),
                a.getMbr(),
                a.getHepMBR(),
                a.getHepMBRWater(),
                a.getMjernoMjesto(),
                a.getDecimalno(),
                toDto(a.getPerson())
        );
    }

    public static DTO.ApartmentRowDto toRowDto(Apartment a, long metersCount, long waterMetersCount) {
        return new DTO.ApartmentRowDto(
                a.getId(),
                a.getApartmentNumber(),
                Boolean.TRUE.equals(a.getActive()),
                a.getPriority(),
                a.getSequence(),
                a.getMbr(),
                a.getHepMBR(),
                a.getHepMBRWater(),
                a.getMjernoMjesto(),
                toDto(a.getPerson()),
                metersCount,
                waterMetersCount
        );
    }

    /* -------- Meter -------- */

    public static DTO.MeterDto toDto(Meter m) {
        return new DTO.MeterDto(
                m.getId(),
                m.getCode(),
                m.getPower(),
                Boolean.TRUE.equals(m.getActive()),
                m.getInstallationDate() // <-- već je LocalDate
        );
    }

    public static DTO.WaterMeterDto toDto(WaterMeter m) {
        return new DTO.WaterMeterDto(
                m.getId(),
                m.getCode(),
                m.getPower(),
                Boolean.TRUE.equals(m.getActive()),
                m.getInstallationDate(), // <-- već je LocalDate
                m.getWaterMeterType() != null ? m.getWaterMeterType().name() : null
        );
    }

    /* -------- Measurement -------- */

    public static DTO.MeasurementDto toDto(Measurement m) {
        return new DTO.MeasurementDto(
                m.getId(),
                m.getMeasureDate(),
                m.getValue(),
                m.getCreatedAt(),
                m.getCreatedBy()
        );
    }

    public static DTO.PersonDto toPersonDto(Person p) {
        if (p == null) return null;
        return new DTO.PersonDto(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getContact()
        );
    }



}
