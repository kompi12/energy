package com.example.energy.viewmodel.dto.mapper;

import com.example.energy.model.Building;
import com.example.energy.model.report.*;
import com.example.energy.viewmodel.dto.report.DispatchDTO;

public class DispatchMapper {

    public static DispatchDTO.BatchDto toDto(MonthlyDispatchBatch b) {
        return new DispatchDTO.BatchDto(
                b.getId(),
                b.getDispatchType(),
                b.getMonthYm(),
                b.getCreatedAt(),
                b.getCreatedBy()
        );
    }

    public static DispatchDTO.ItemDto toDto(MonthlyDispatchItem i) {
        Building b = i.getBuilding();

        String city = b.getCity() != null ? b.getCity().getName() : null;
        String addr = (b.getAddresses() != null && !b.getAddresses().isEmpty())
                ? b.getAddresses().get(0).getAddressLine()
                : null;

        return new DispatchDTO.ItemDto(
                i.getId(),
                b.getId(),
                b.getCode(),
                b.getName(),
                city,
                addr,
                i.getStatus(),
                i.getLastNote(),
                i.getUpdatedAt(),
                i.getUpdatedBy()
        );
    }

    public static DispatchDTO.EventDto toDto(MonthlyDispatchEvent e) {
        return new DispatchDTO.EventDto(
                e.getId(),
                e.getEventType(),
                e.getNote(),
                e.getFromStatus(),
                e.getToStatus(),
                e.getCreatedAt(),
                e.getCreatedBy()
        );
    }
}
