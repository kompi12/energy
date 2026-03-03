package com.example.energy.viewmodel.dto;

public record DeviceSearchResultDto(
        String deviceType,        // "METER" ili "WATER"
        Long deviceId,
        String code,
        Boolean active,
        Long apartmentId,
        String apartmentNumber,
        String buildingCode
) {}
