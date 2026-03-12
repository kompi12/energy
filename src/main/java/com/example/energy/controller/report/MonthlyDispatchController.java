package com.example.energy.controller.report;

import com.example.energy.model.report.DispatchStatus;
import com.example.energy.model.report.DispatchType;
import com.example.energy.service.report.MonthlyDispatchService;
import com.example.energy.viewmodel.dto.report.DispatchDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
public class MonthlyDispatchController {

    private final MonthlyDispatchService service;

    public MonthlyDispatchController(MonthlyDispatchService service) {
        this.service = service;
    }

    // -------------------------
    // NEW (za tvoj frontend)
    // -------------------------

    // LIST batches (frontend: GET /api/dispatch/batches?type=&year=&month=)
    @GetMapping("/batches")
    public List<DispatchDTO.BatchDto> listBatches(
            @RequestParam(required = false) DispatchType type,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return service.listBatches(type, year, month);
    }

    // CREATE batch (frontend: POST /api/dispatch/batches)
    @PostMapping("/batches")
    public DispatchDTO.BatchDto createBatch(@RequestBody DispatchDTO.CreateBatchRequest req) {
        return service.createBatch(req);
    }

    // PAGE ITEMS (frontend: GET /api/dispatch/batches/{batchId}/items)
    @GetMapping("/batches/{batchId}/items")
    public DispatchDTO.PageResult<DispatchDTO.ItemDto> pageItemsV2(
            @PathVariable Long batchId,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.pageItems(batchId, status, city, q, page, size);
    }

    // GET ITEM + EVENTS (frontend: GET /api/dispatch/items/{itemId})
    @GetMapping("/items/{itemId}")
    public DispatchDTO.ItemDetailDto getItem(@PathVariable Long itemId) {
        return service.getItemDetail(itemId);
    }

    // ADD EVENT (frontend: POST /api/dispatch/items/{itemId}/events)
    @PostMapping("/items/{itemId}/events")
    public DispatchDTO.EventDto addEventV2(
            @PathVariable Long itemId,
            @RequestBody DispatchDTO.AddEventRequest req
    ) {
        return service.addEvent(itemId, req);
    }

    // PATCH STATUS (frontend: PATCH /api/dispatch/items/{itemId}/status)
    @PatchMapping("/items/{itemId}/status")
    public DispatchDTO.ItemDto setStatus(
            @PathVariable Long itemId,
            @RequestBody DispatchDTO.SetStatusRequest req
    ) {
        return service.setStatus(itemId, req.status());
    }

    // -------------------------
    // OLD (tvoje postojeće rute) - ostaju
    // -------------------------

    @GetMapping("/batch")
    public DispatchDTO.BatchDto getOrCreateBatch(
            @RequestParam DispatchType type,
            @RequestParam String monthYm
    ) {
        return service.getOrCreateBatch(type, monthYm);
    }

    @PostMapping("/batch/{batchId}/init-items")
    public DispatchDTO.InitBatchItemsResponse initItems(@PathVariable Long batchId) {
        return service.initItemsForAllBuildings(batchId);
    }

    @GetMapping("/batch/{batchId}/items")
    public DispatchDTO.PageResult<DispatchDTO.ItemDto> pageItems(
            @PathVariable Long batchId,
            @RequestParam(required = false) DispatchStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.pageItems(batchId, status, city, q, page, size);
    }

    @PostMapping("/batch/{batchId}/item")
    public DispatchDTO.ItemDto upsertItem(
            @PathVariable Long batchId,
            @RequestBody DispatchDTO.UpsertItemRequest req
    ) {
        return service.upsertItem(batchId, req);
    }

    @PostMapping("/item/{itemId}/fix-send")
    public DispatchDTO.ItemDto fixAndSend(
            @PathVariable Long itemId,
            @RequestBody DispatchDTO.FixAndSendRequest req
    ) {
        return service.fixAndSend(itemId, req);
    }

    @PostMapping("/item/{itemId}/event")
    public DispatchDTO.EventDto addEvent(
            @PathVariable Long itemId,
            @RequestBody DispatchDTO.AddEventRequest req
    ) {
        return service.addEvent(itemId, req);
    }

    @GetMapping("/item/{itemId}/timeline")
    public List<DispatchDTO.EventDto> timeline(@PathVariable Long itemId) {
        return service.timeline(itemId);
    }
}
