package com.example.energy.service.report;

import com.example.energy.model.Building;
import com.example.energy.model.report.*;
import com.example.energy.repository.BuildingRepository;
import com.example.energy.repository.report.MonthlyDispatchBatchRepository;
import com.example.energy.repository.report.MonthlyDispatchEventRepository;
import com.example.energy.repository.report.MonthlyDispatchItemRepository;
import com.example.energy.viewmodel.dto.mapper.DispatchMapper;
import com.example.energy.viewmodel.dto.report.DispatchDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Service
public class MonthlyDispatchService {

    private final MonthlyDispatchBatchRepository batchRepo;
    private final MonthlyDispatchItemRepository itemRepo;
    private final MonthlyDispatchEventRepository eventRepo;
    private final BuildingRepository buildingRepo;

    public MonthlyDispatchService(
            MonthlyDispatchBatchRepository batchRepo,
            MonthlyDispatchItemRepository itemRepo,
            MonthlyDispatchEventRepository eventRepo,
            BuildingRepository buildingRepo
    ) {
        this.batchRepo = batchRepo;
        this.itemRepo = itemRepo;
        this.eventRepo = eventRepo;
        this.buildingRepo = buildingRepo;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // -----------------------------------------
    // ✅ NEW methods for frontend (/batches, /items/{id}, /status)
    // -----------------------------------------

    @Transactional
    public List<DispatchDTO.BatchDto> listBatches(DispatchType type, Integer year, Integer month) {
        // Ako year/month dođu, pretvaramo u monthYm; inače listamo sve (ili po type)
        String monthYm = null;
        if (year != null && month != null) {
            monthYm = YearMonth.of(year, month).toString(); // "YYYY-MM"
        }

        List<MonthlyDispatchBatch> list;
        if (type != null && monthYm != null) {
            list = batchRepo.findAllByDispatchTypeAndMonthYmOrderByIdDesc(type, monthYm);
        } else if (type != null) {
            list = batchRepo.findAllByDispatchTypeOrderByIdDesc(type);
        } else if (monthYm != null) {
            list = batchRepo.findAllByMonthYmOrderByIdDesc(monthYm);
        } else {
            list = batchRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }

        return list.stream().map(DispatchMapper::toDto).toList();
    }

    @Transactional
    public DispatchDTO.BatchDto createBatch(DispatchDTO.CreateBatchRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is required");
        if (req.dispatchType() == null) throw new IllegalArgumentException("dispatchType is required");
        if (req.monthYm() == null || req.monthYm().isBlank()) throw new IllegalArgumentException("monthYm is required");

        // validate YYYY-MM
        YearMonth ym = YearMonth.parse(req.monthYm().trim());

        // enforce unique: type + monthYm
        MonthlyDispatchBatch batch = batchRepo.findByDispatchTypeAndMonthYm(req.dispatchType(), ym.toString())
                .orElseGet(() -> batchRepo.save(new MonthlyDispatchBatch(req.dispatchType(), ym, currentUsername())));

        return DispatchMapper.toDto(batch);
    }

    @Transactional
    public DispatchDTO.ItemDetailDto getItemDetail(Long itemId) {
        MonthlyDispatchItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        List<DispatchDTO.EventDto> events = eventRepo.findTimeline(itemId)
                .stream()
                .map(DispatchMapper::toDto)
                .toList();

        return new DispatchDTO.ItemDetailDto(DispatchMapper.toDto(item), events);
    }

    @Transactional
    public DispatchDTO.ItemDto setStatus(Long itemId, DispatchStatus status) {
        if (status == null) throw new IllegalArgumentException("status is required");

        MonthlyDispatchItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        DispatchStatus prev = item.getStatus();
        if (prev == status) {
            // ništa za mijenjat, ali možemo vratit DTO
            return DispatchMapper.toDto(item);
        }

        item.setStatus(status);
        item.setUpdatedAt(Instant.now());
        item.setUpdatedBy(currentUsername());
        itemRepo.save(item);

        MonthlyDispatchEvent ev = new MonthlyDispatchEvent();
        ev.setItem(item);
        ev.setEventType(DispatchEventType.STATUS_CHANGE);
        ev.setFromStatus(prev);
        ev.setToStatus(status);
        ev.setNote("Status changed");
        ev.setCreatedAt(Instant.now());
        ev.setCreatedBy(currentUsername());
        eventRepo.save(ev);

        return DispatchMapper.toDto(item);
    }

    // -----------------------------------------
    // Existing methods (tvoje) - keep
    // -----------------------------------------

    @Transactional
    public MonthlyDispatchBatch getOrCreateBatchEntity(DispatchType type, String monthYm) {
        YearMonth.parse(monthYm);
        return batchRepo.findByDispatchTypeAndMonthYm(type, monthYm)
                .orElseGet(() -> batchRepo.save(new MonthlyDispatchBatch(type, YearMonth.parse(monthYm), currentUsername())));
    }

    @Transactional
    public DispatchDTO.BatchDto getOrCreateBatch(DispatchType type, String monthYm) {
        return DispatchMapper.toDto(getOrCreateBatchEntity(type, monthYm));
    }

    // ✅ 1) Auto-create items for all buildings
    @Transactional
    public DispatchDTO.InitBatchItemsResponse initItemsForAllBuildings(Long batchId) {
        MonthlyDispatchBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        List<Building> buildings = buildingRepo.findAllForDispatch();

        long created = 0;
        long existed = 0;

        for (Building b : buildings) {
            var existing = itemRepo.findByBatch_IdAndBuilding_Id(batch.getId(), b.getId()).orElse(null);
            if (existing != null) {
                existed++;
                continue;
            }

            MonthlyDispatchItem item = new MonthlyDispatchItem();
            item.setBatch(batch);
            item.setBuilding(b);
            item.setStatus(DispatchStatus.NOT_SENT);
            item.setUpdatedAt(Instant.now());
            item.setUpdatedBy(currentUsername());
            itemRepo.save(item);

            MonthlyDispatchEvent ev = new MonthlyDispatchEvent();
            ev.setItem(item);
            ev.setEventType(DispatchEventType.NOTE);
            ev.setNote("Auto-created for batch");
            ev.setCreatedAt(Instant.now());
            ev.setCreatedBy(currentUsername());
            eventRepo.save(ev);

            created++;
        }

        return new DispatchDTO.InitBatchItemsResponse(created, existed);
    }

    // ✅ 2) Pagination + filters
    @Transactional
    public DispatchDTO.PageResult<DispatchDTO.ItemDto> pageItems(
            Long batchId,
            DispatchStatus status,
            String city,
            String q,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 200);

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());

        Page<MonthlyDispatchItem> p = itemRepo.pageByBatch(
                batchId,
                status,
                (city == null || city.isBlank()) ? null : city.trim(),
                (q == null || q.isBlank()) ? null : q.trim(),
                pageable
        );

        List<DispatchDTO.ItemDto> items = p.getContent().stream().map(DispatchMapper::toDto).toList();

        return new DispatchDTO.PageResult<>(
                items,
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages()
        );
    }

    // ✅ 3) Fix flow: ERROR -> FIXED_SENT (requires fixNote)
    @Transactional
    public DispatchDTO.ItemDto fixAndSend(Long itemId, DispatchDTO.FixAndSendRequest req) {
        if (req == null || req.fixNote() == null || req.fixNote().trim().isEmpty()) {
            throw new IllegalArgumentException("fixNote is required");
        }

        MonthlyDispatchItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        DispatchStatus prev = item.getStatus();

        item.setLastNote(req.fixNote().trim());
        item.setStatus(DispatchStatus.FIXED_SENT);
        item.setUpdatedAt(Instant.now());
        item.setUpdatedBy(currentUsername());
        itemRepo.save(item);

        MonthlyDispatchEvent ev = new MonthlyDispatchEvent();
        ev.setItem(item);
        ev.setEventType(DispatchEventType.FIX_APPLIED);
        ev.setNote(req.fixNote().trim());
        ev.setFromStatus(prev);
        ev.setToStatus(item.getStatus());
        ev.setCreatedAt(Instant.now());
        ev.setCreatedBy(currentUsername());
        eventRepo.save(ev);

        return DispatchMapper.toDto(item);
    }

    // (postojeće) upsert item: status + note
    @Transactional
    public DispatchDTO.ItemDto upsertItem(Long batchId, DispatchDTO.UpsertItemRequest req) {
        MonthlyDispatchBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchId));

        Building building = buildingRepo.findById(req.buildingId())
                .orElseThrow(() -> new IllegalArgumentException("Building not found: " + req.buildingId()));

        MonthlyDispatchItem item = itemRepo.findByBatch_IdAndBuilding_Id(batchId, building.getId()).orElse(null);

        DispatchStatus prev = null;

        if (item == null) {
            item = new MonthlyDispatchItem();
            item.setBatch(batch);
            item.setBuilding(building);
            item.setStatus(req.status() != null ? req.status() : DispatchStatus.NOT_SENT);
        } else {
            prev = item.getStatus();
            if (req.status() != null) item.setStatus(req.status());
        }

        if (req.note() != null && !req.note().isBlank()) item.setLastNote(req.note().trim());

        item.setUpdatedAt(Instant.now());
        item.setUpdatedBy(currentUsername());
        itemRepo.save(item);

        MonthlyDispatchEvent ev = new MonthlyDispatchEvent();
        ev.setItem(item);
        ev.setCreatedAt(Instant.now());
        ev.setCreatedBy(currentUsername());

        if (prev != null && req.status() != null && prev != req.status()) {
            ev.setEventType(DispatchEventType.STATUS_CHANGE);
            ev.setFromStatus(prev);
            ev.setToStatus(req.status());
            ev.setNote(req.note());
        } else {
            ev.setEventType(DispatchEventType.NOTE);
            ev.setNote(req.note());
        }
        eventRepo.save(ev);

        return DispatchMapper.toDto(item);
    }

    @Transactional
    public DispatchDTO.EventDto addEvent(Long itemId, DispatchDTO.AddEventRequest req) {
        MonthlyDispatchItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        DispatchStatus prev = item.getStatus();

        if (req.toStatus() != null && req.toStatus() != prev) {
            item.setStatus(req.toStatus());
            item.setUpdatedAt(Instant.now());
            item.setUpdatedBy(currentUsername());
            if (req.note() != null && !req.note().isBlank()) item.setLastNote(req.note().trim());
            itemRepo.save(item);
        }

        MonthlyDispatchEvent ev = new MonthlyDispatchEvent();
        ev.setItem(item);
        ev.setEventType(req.eventType() != null ? req.eventType() : DispatchEventType.NOTE);
        ev.setNote(req.note());
        ev.setFromStatus(prev);
        ev.setToStatus(item.getStatus());
        ev.setCreatedAt(Instant.now());
        ev.setCreatedBy(currentUsername());
        eventRepo.save(ev);

        return DispatchMapper.toDto(ev);
    }

    @Transactional
    public List<DispatchDTO.EventDto> timeline(Long itemId) {
        return eventRepo.findTimeline(itemId).stream().map(DispatchMapper::toDto).toList();
    }
}
