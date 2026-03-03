package com.example.energy.service;

import com.example.energy.model.Apartment;
import com.example.energy.model.Meter;
import com.example.energy.model.Person;
import com.example.energy.model.WaterMeter;
import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.repository.PersonRepository;
import com.example.energy.repository.WaterMeterRepository;
import com.example.energy.service.audit.AuditService;
import com.example.energy.viewmodel.ApartmentViewModel;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.UpdateApartmentRequest;
import com.example.energy.viewmodel.dto.mapper.DTOMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepo;
    private final MeterRepository meterRepo;
    private final WaterMeterRepository waterMeterRepo;
    private final AuditService auditService;
    private final PersonRepository personRepo;

    public ApartmentService(ApartmentRepository apartmentRepo,
                            MeterRepository meterRepo,
                            WaterMeterRepository waterMeterRepo,AuditService auditService,PersonRepository personRepo) {
        this.apartmentRepo = apartmentRepo;
        this.meterRepo = meterRepo;
        this.waterMeterRepo = waterMeterRepo;
        this.auditService = auditService;
        this.personRepo = personRepo;
    }
    public List<ApartmentViewModel> getApartmentByPersonName(String name) {
        List<ApartmentViewModel> apartmentViewModels = new ArrayList<>();
        List<Apartment> apartment = apartmentRepo.findApartmentsByPersonFirstNameLike(name);
        apartment.forEach(a ->apartmentViewModels.add(ApartmentViewModel.createViewModel(a)));
        return apartmentViewModels;
    }
    public List<Apartment> findAll() {
        return apartmentRepo.findAll();
    }

    @Transactional(readOnly = true)
    public DTO.ApartmentDto getApartment(Long id) {
        Apartment a = apartmentRepo.findByIdWithPerson(id)
                .orElseThrow(() -> new EntityNotFoundException("Apartment not found"));
        return DTOMapper.toDto(a);
    }

    @Transactional
    public DTO.ApartmentDto updateApartment(Long id, DTO.ApartmentUpdateRequest req) {
        Apartment a = apartmentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Apartment not found"));

        var oldSnap = new java.util.LinkedHashMap<String, Object>();
        oldSnap.put("active", a.getActive());
        oldSnap.put("priority", a.getPriority());
        oldSnap.put("sequence", a.getSequence());
        oldSnap.put("mbr", a.getMbr());
        oldSnap.put("hepMBR", a.getHepMBR());
        oldSnap.put("hepMBRWater", a.getHepMBRWater());
        oldSnap.put("mjernoMjesto", a.getMjernoMjesto());
        oldSnap.put("decimalno", a.getDecimalno());

        if (req.active() != null) a.setActive(req.active());

        a.setPriority(req.priority());
        a.setSequence(req.sequence());
        a.setMbr(req.mbr());
        a.setHepMBR(req.hepMBR());
        a.setHepMBRWater(req.hepMBRWater());
        a.setMjernoMjesto(req.mjernoMjesto());
        a.setDecimalno(req.decimalno());

        apartmentRepo.save(a);

        var newSnap = new java.util.LinkedHashMap<String, Object>();
        newSnap.put("active", a.getActive());
        newSnap.put("priority", a.getPriority());
        newSnap.put("sequence", a.getSequence());
        newSnap.put("mbr", a.getMbr());
        newSnap.put("hepMBR", a.getHepMBR());
        newSnap.put("hepMBRWater", a.getHepMBRWater());
        newSnap.put("mjernoMjesto", a.getMjernoMjesto());
        newSnap.put("decimalno", a.getDecimalno());

        if (!oldSnap.equals(newSnap)) {
            auditService.logEntityChange("ENTITY_UPDATE", "Apartment", String.valueOf(a.getId()), oldSnap, newSnap);
        }

        return DTOMapper.toDto(a);
    }

    // trik: ako želiš dopustiti slanje null (da obrišeš polje), treba ti custom request parsing.
    // Ovdje minimalno: pretpostavljamo da frontend šalje null kad želi set null.
    private boolean hasFieldInRequest(DTO.ApartmentUpdateRequest req, String field) {
        // ako koristiš Jackson + record, nema direktno "field present" bez custom wrappera.
        // Za demo: vrati false.
        return false;
    }

    @Transactional(readOnly = true)
    public DTO.ApartmentDevicesDto getDevices(Long apartmentId) {
        List<DTO.MeterDto> meters = meterRepo.findByApartment_Id(apartmentId).stream().map(DTOMapper::toDto).toList();
        List<DTO.WaterMeterDto> waterMeters = waterMeterRepo.findByApartment_Id(apartmentId).stream().map(DTOMapper::toDto).toList();
        return new DTO.ApartmentDevicesDto(meters, waterMeters);
    }

    @Transactional
    public DTO.MeterDto updateMeter(Long id, DTO.DeviceUpdateRequest req) {
        Meter m = meterRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Meter not found"));


        var oldSnap = new java.util.LinkedHashMap<String, Object>();
        oldSnap.put("active", m.getActive());
        oldSnap.put("power", m.getPower());


        if (req.active() != null) m.setActive(req.active());
        if (req.power() != null) m.setPower(req.power());

        meterRepo.save(m);



        var newSnap = new java.util.LinkedHashMap<String, Object>();
        newSnap.put("active", m.getActive());
        newSnap.put("power", m.getPower());

        if (!oldSnap.equals(newSnap)) {
            auditService.logEntityChange("ENTITY_UPDATE", "Meter", String.valueOf(m.getId()), oldSnap, newSnap);
        }

        return DTOMapper.toDto(m);
    }

    @Transactional
    public DTO.WaterMeterDto updateWaterMeter(Long id, DTO.DeviceUpdateRequest req) {
        WaterMeter m = waterMeterRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("WaterMeter not found"));


        var oldSnap = new java.util.LinkedHashMap<String, Object>();
        oldSnap.put("active", m.getActive());
        oldSnap.put("power", m.getPower());

        if (req.active() != null) m.setActive(req.active());
        if (req.power() != null) m.setPower(req.power());

        waterMeterRepo.save(m);

        var newSnap = new java.util.LinkedHashMap<String, Object>();
        newSnap.put("active", m.getActive());
        newSnap.put("power", m.getPower());

        if (!oldSnap.equals(newSnap)) {
            auditService.logEntityChange("ENTITY_UPDATE", "WaterMeter", String.valueOf(m.getId()), oldSnap, newSnap);
        }

        return DTOMapper.toDto(m);
    }

    @Transactional
    public DTO.MeterDto addMeterToApartment(Long aptId, DTO.MeterCreateRequest req) {
        Apartment apt = apartmentRepo.findById(aptId)
                .orElseThrow(() -> new RuntimeException("Apartment not found: " + aptId));

        String code = req.code() == null ? "" : req.code().trim();
        if (code.isEmpty()) throw new RuntimeException("Code is required");

        if (meterRepo.existsByCodeIgnoreCase(code)) {
            throw new RuntimeException("Meter with code already exists: " + code);
        }

        Meter m = new Meter();
        m.setApartment(apt);
        m.setCode(code);
        m.setPower(req.power() == null || req.power().trim().isEmpty() ? null : req.power().trim());
        m.setInstallationDate(req.installationDate());
        m.setActive(req.active() != null ? req.active() : Boolean.TRUE);

        Meter saved = meterRepo.save(m);

        auditService.logEntityChange("ENTITY_CREATE", "meter", String.valueOf(saved.getId()),String.valueOf( saved),null);


        return new DTO.MeterDto(
                saved.getId(),
                saved.getCode(),
                saved.getPower(),
                Boolean.TRUE.equals(saved.getActive()),
                saved.getInstallationDate()
        );
    }

    @Transactional
    public DTO.WaterMeterDto addWaterMeterToApartment(Long aptId, DTO.WaterMeterCreateRequest req) {
        Apartment apt = apartmentRepo.findById(aptId)
                .orElseThrow(() -> new RuntimeException("Apartment not found: " + aptId));

        String code = req.code() == null ? "" : req.code().trim();
        if (code.isEmpty()) throw new RuntimeException("Code is required");

        if (req.waterMeterType() == null) {
            throw new RuntimeException("waterMeterType is required");
        }

        if (waterMeterRepo.existsByCodeIgnoreCase(code)) {
            throw new RuntimeException("Water meter with code already exists: " + code);
        }

        WaterMeter wm = new WaterMeter();
        wm.setApartment(apt);
        wm.setCode(code);
        wm.setPower(req.power() == null || req.power().trim().isEmpty() ? null : req.power().trim());
        wm.setInstallationDate(req.installationDate());
        wm.setWaterMeterType(req.waterMeterType());
        wm.setActive(req.active() != null ? req.active() : Boolean.TRUE);

        WaterMeter saved = waterMeterRepo.save(wm);
        auditService.logEntityChange("ENTITY_CREATE", "waterMeter", String.valueOf(saved.getId()),String.valueOf( saved),null);

        return new DTO.WaterMeterDto(
                saved.getId(),
                saved.getCode(),
                saved.getPower(),
                Boolean.TRUE.equals(saved.getActive()),
                saved.getInstallationDate(),
                saved.getWaterMeterType().name() // string kako ti DTO već ima
        );
    }


    @Transactional
    public DTO.ApartmentDto updateApartment(Long apartmentId, UpdateApartmentRequest req) {
        Apartment a = apartmentRepo.findById(apartmentId)
                .orElseThrow(() -> new EntityNotFoundException("Apartment not found: " + apartmentId));

        if (req.apartmentNumber != null) a.setApartmentNumber(req.apartmentNumber);
        if (req.active != null) a.setActive(req.active);
        a.setPriority(req.priority);
        a.setSequence(req.sequence);
        a.setMbr(req.mbr);
        a.setHepMBR(req.hepMBR);
        a.setHepMBRWater(req.hepMBRWater);
        a.setMjernoMjesto(req.mjernoMjesto);
        a.setDecimalno(req.decimalno);

        // Person handling
        if (Boolean.TRUE.equals(req.clearPerson)) {
            a.setPerson(null);
        } else if (req.personId != null) {
            Person p = personRepo.findById(req.personId)
                    .orElseThrow(() -> new EntityNotFoundException("Person not found: " + req.personId));
            a.setPerson(p);
        } else if (req.person != null && req.person.firstName != null && !req.person.firstName.trim().isEmpty()) {
            Person p = new Person();
            p.setFirstName(req.person.firstName.trim());
            p.setLastName(req.person.lastName);
            p.setContact(req.person.contact);
            p = personRepo.save(p);
            a.setPerson(p);
        }

        Apartment saved = apartmentRepo.save(a);
        return DTOMapper.toDto(saved);
    }
}
