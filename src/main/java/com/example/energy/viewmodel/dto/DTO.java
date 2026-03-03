package com.example.energy.viewmodel.dto;
import com.example.energy.enums.WaterMeterType;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
public class DTO {

    private DTO() {}

    // package com.example.energy.api.dto;


    public record BuildingDto(
            Long id,
            String code,
            String name,
            String techem,
            String cityName,
            List<AddressDto> addresses
    ) {

        public record AddressDto(
                String addressLine,
                String postalCode,
                String city,
                String country
        ) {}
    }

    public record PersonDto(
            Long id,
            String firstName,
            String lastName,
            String contact
    ) {}

    public record ApartmentRowDto(
            Long id,
            String apartmentNumber,
            boolean active,
            Integer priority,
            Integer sequence,
            String mbr,
            String hepMBR,
            String hepMBRWater,
            String mjernoMjesto,
            PersonDto person,
            long metersCount,
            long waterMetersCount
    ) {}

    public record ApartmentDto(
            Long id,
            String apartmentNumber,
            boolean active,
            Integer priority,
            Integer sequence,
            String mbr,
            String hepMBR,
            String hepMBRWater,
            String mjernoMjesto,
            Double decimalno,
            PersonDto person
    ) {}

    public record ApartmentUpdateRequest(
            Boolean active,
            Integer priority,
            Integer sequence,
            String mbr,
            String hepMBR,
            String hepMBRWater,
            String mjernoMjesto,
            Double decimalno
    ) {}

    public record MeterDto(
            Long id,
            String code,
            String power,
            boolean active,
            LocalDate installationDate
    ) {}

    public record WaterMeterDto(
            Long id,
            String code,
            String power,
            boolean active,
            LocalDate installationDate,
            String waterMeterType
    ) {}

    public record ApartmentDevicesDto(
            List<MeterDto> meters,
            List<WaterMeterDto> waterMeters
    ) {}

    public record DeviceUpdateRequest(
            Boolean active,
            String power
    ) {}

    public record MeasurementDto(
            Long id,
            LocalDate measureDate,
            Double value,
            Instant createdAt,
            String createdBy
    ) {}

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

    public record DeviceSearchResultDto(
            String type,          // "WATER_METER" | "METER"
            Long deviceId,
            String code,
            Boolean active,
            Long apartmentId,
            String apartmentNumber,
            String buildingCode
    ) {}


    public record MeasurementCreateRequest(
            LocalDate measureDate,
            Double value
    ) {}

    public record BuildingSummaryDto(
            long apartments,
            long persons,
            long meters,
            long waterMeters
    ) {}

    public record MeterCreateRequest(
            String code,
            String power,
            LocalDate installationDate,
            Boolean active
    ) {}

    public record WaterMeterCreateRequest(
            String code,
            String power,
            LocalDate installationDate,
            WaterMeterType waterMeterType, // <-- enum direktno
            Boolean active
    ) {}

    public record ImportResult(
            String type,                 // "XML" / "TECHEM"
            int createdMeasurements,     // XML
            int inserted,                // Excel
            int duplicates,
            int missingMeter,
            int skipped,
            java.util.List<String> warnings
    ) {}

}
