package com.example.energy.viewmodel.dto.report;

import com.example.energy.model.report.*;

import java.time.Instant;
import java.util.List;

public class DispatchDTO {

    public record BatchDto(
            Long id,
            DispatchType dispatchType,
            String monthYm,
            Instant createdAt,
            String createdBy
    ) {}

    public record ItemDto(
            Long id,
            Long buildingId,
            String buildingCode,
            String buildingName,
            String buildingCity,
            String buildingAddress,
            DispatchStatus status,
            String lastNote,
            Instant updatedAt,
            String updatedBy
    ) {}

    public record EventDto(
            Long id,
            DispatchEventType eventType,
            String note,
            DispatchStatus fromStatus,
            DispatchStatus toStatus,
            Instant createdAt,
            String createdBy
    ) {}

    public record BatchWithItemsDto(
            BatchDto batch,
            List<ItemDto> items
    ) {}

    // ✅ NEW: item + events (za GET /items/{id})
    public record ItemDetailDto(
            ItemDto item,
            List<EventDto> events
    ) {}

    // requests
    public record CreateBatchRequest(
            DispatchType dispatchType,
            String monthYm // "YYYY-MM"
    ) {}

    public record UpsertItemRequest(
            Long buildingId,
            DispatchStatus status,
            String note
    ) {}

    public record AddEventRequest(
            DispatchEventType eventType,
            String note,
            DispatchStatus toStatus // optional, koristi kad želiš promijeniti status kroz event
    ) {}

    // ✅ NEW: PATCH /items/{id}/status
    public record SetStatusRequest(DispatchStatus status) {}

    public record InitBatchItemsResponse(
            long created,
            long alreadyExisted
    ) {}

    public record PageResult<T>(
            java.util.List<T> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record FixAndSendRequest(
            String fixNote // obavezno
    ) {}
}
