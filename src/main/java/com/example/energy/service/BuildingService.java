package com.example.energy.service;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.viewmodel.dto.CreateApartmentRequest;
import com.example.energy.viewmodel.dto.CreateBuildingRequest;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.mapper.DTOMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BuildingService {
    private final BuildingRepository buildingRepo;
    private final ApartmentRepository apartmentRepo;
    private final MeterRepository meterRepo;
    private final WaterMeterRepository waterMeterRepo;
    private final CityRepository cityRepository;
    private final PersonRepository personRepo;

    public BuildingService(BuildingRepository buildingRepo,
                           ApartmentRepository apartmentRepo,
                           MeterRepository meterRepo,
                           WaterMeterRepository waterMeterRepo, CityRepository cityRepository,PersonRepository personRepo) {
        this.buildingRepo = buildingRepo;
        this.apartmentRepo = apartmentRepo;
        this.meterRepo = meterRepo;
        this.waterMeterRepo = waterMeterRepo;
        this.cityRepository = cityRepository;
        this.personRepo = personRepo;
    }

    public List<Building> findAll() {
        return buildingRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<DTO.BuildingDto> listBuildings() {
        return buildingRepo.findAll().stream().map(DTOMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public DTO.BuildingDto getBuilding(Long id) {
        Building b = buildingRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Building not found"));
        return DTOMapper.toDto(b);
    }

    @Transactional(readOnly = true)
    public DTO.BuildingSummaryDto getSummary(Long buildingId) {
        long apartments = apartmentRepo.countByBuilding_Id(buildingId);
        long persons = apartmentRepo.countDistinctPersonsInBuilding(buildingId);
        long meters = meterRepo.countByApartment_Building_Id(buildingId);
        long waterMeters = waterMeterRepo.countByApartment_Building_Id(buildingId);
        return new DTO.BuildingSummaryDto(apartments, persons, meters, waterMeters);
    }

//    @Transactional(readOnly = true)
//    public List<DTO.ApartmentRowDto> listApartmentsForBuilding(Long buildingId) {
//        List<Apartment> apts = apartmentRepo.findByBuildingWithPerson(buildingId);
//
//        // counts bez dodatnih endpointa (brzo i jednostavno)
//        // ako ti je puno stanova, možeš napraviti 2 agregacijska queryja umjesto ovoga.
//        return apts.stream()
//                .map(a -> DTOMapper.toRowDto(
//                        a,
//                        a.getMeters() != null ? a.getMeters().size() : 0,
//                        a.getWaterMeters() != null ? a.getWaterMeters().size() : 0
//                ))
//                .toList();
//    }

    public List<DTO.BuildingDto> getAllBuildings() {
        return buildingRepo.findAllWithAddresses()
                .stream()
                .map(DTOMapper::toDto)
                .toList();
    }


    public List<DTO.BuildingDto> getAllBuildingsHeater(DTO.RequestBodyHeat body) {
        return buildingRepo.findAllWithAddresses()
                .stream()
                .filter(body.heat() ? building -> building.getHeat().equals(true) : building -> building.getWater().equals(true) )
                .map(DTOMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DTO.ApartmentRowDto> listApartmentsForBuilding(Long buildingId) {

        List<Apartment> apts = apartmentRepo.findForBuildingWithPerson(buildingId);

        Map<Long, Long> metersCount = meterRepo.countActiveByApartmentForBuilding(buildingId)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        Map<Long, Long> waterMetersCount = waterMeterRepo.countActiveByApartmentForBuilding(buildingId)
                .stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> (Long) r[1]
                ));

        return apts.stream()
                .sorted(Comparator
                        .comparing((Apartment a) -> a.getSequence() == null ? Integer.MAX_VALUE : a.getSequence())
                        .thenComparing(Apartment::getId))
                .map(a -> new DTO.ApartmentRowDto(
                        a.getId(),
                        a.getApartmentNumber(),
                        Boolean.TRUE.equals(a.getActive()),
                        a.getPriority(),
                        a.getSequence(),
                        a.getMbr(),
                        a.getHepMBR(),
                        a.getHepMBRWater(),
                        a.getMjernoMjesto(),
                        DTOMapper.toPersonDto(a.getPerson()), // ili null-safe
                        metersCount.getOrDefault(a.getId(), 0L),
                        waterMetersCount.getOrDefault(a.getId(), 0L)
                ))
                .toList();
    }

    @Transactional
    public DTO.BuildingDto createFull(CreateBuildingRequest req) {
        if (req == null) throw new IllegalArgumentException("Request is null");
        if (req.code == null || req.code.trim().isEmpty()) throw new IllegalArgumentException("code is required");
        if (req.cityId == null) throw new IllegalArgumentException("cityId is required");

        City city = cityRepository.findById(req.cityId)
                .orElseThrow(() -> new EntityNotFoundException("City not found: " + req.cityId));

        Building b = new Building();
        b.setCode(req.code.trim());
        b.setName(req.name);
        b.setTechem(req.techem);
        b.setCity(city);

        // --- addresses
        if (req.addresses != null) {
            for (CreateBuildingRequest.AddressIn a : req.addresses) {
                if (a == null) continue;
                if (a.addressLine == null || a.addressLine.trim().isEmpty()) continue;

                BuildingAddress ba = new BuildingAddress();
                ba.setBuilding(b);
                ba.setAddressLine(a.addressLine.trim());
                ba.setPostalCode(emptyToNull(a.postalCode));
                ba.setCity(emptyToNull(a.city));
                ba.setCountry(emptyToNull(a.country) != null ? a.country : "HR");

                b.getAddresses().add(ba);
            }
        }

        // --- persons de-dup within this request (optional but practical)
        Map<String, Person> localPersons = new HashMap<>();

        // --- apartments
        if (req.apartments != null) {
            for (CreateBuildingRequest.ApartmentIn a : req.apartments) {
                if (a == null) continue;

                Apartment ap = new Apartment();
                ap.setBuilding(b);
                ap.setApartmentNumber(emptyToNull(a.apartmentNumber));
                ap.setActive(a.active != null ? a.active : Boolean.TRUE);
                ap.setPriority(a.priority);
                ap.setSequence(a.sequence);
                ap.setMbr(emptyToNull(a.mbr));
                ap.setHepMBR(emptyToNull(a.hepMBR));
                ap.setHepMBRWater(emptyToNull(a.hepMBRWater));
                ap.setMjernoMjesto(emptyToNull(a.mjernoMjesto));
                ap.setDecimalno(a.decimalno);

                // person
                if (a.person != null) {
                    Person p = resolvePerson(a.person, localPersons);
                    ap.setPerson(p);
                    // opcionalno: održavanje veze u oba smjera
                    if (p != null) p.getApartments().add(ap);
                }

                b.getApartments().add(ap);
            }
        }

        Building saved = buildingRepo.save(b);

        // IMPORTANT: da DTOMapper vidi addresses/city bez Lazy problema:
        // možeš vratiti DTO direktno iz saved (jer je u istoj transakciji),
        // ili reload sa fetch join.
        return DTOMapper.toDto(saved);
    }

    private Person resolvePerson(CreateBuildingRequest.PersonIn in, Map<String, Person> localPersons) {
        if (in.id != null) {
            return personRepo.findById(in.id)
                    .orElseThrow(() -> new EntityNotFoundException("Person not found: " + in.id));
        }

        String first = emptyToNull(in.firstName);
        if (first == null) return null;

        String key = (first + "|" + nullToEmpty(in.lastName) + "|" + nullToEmpty(in.contact)).toLowerCase().trim();

        if (localPersons.containsKey(key)) return localPersons.get(key);

        Person p = new Person();
        p.setFirstName(first);
        p.setLastName(emptyToNull(in.lastName));
        p.setContact(emptyToNull(in.contact));

        // save odmah (ili će cascade preko apartments ako si postavio cascade; ali kod tebe Person ne cascada na Apartment u ovom smjeru)
        Person saved = personRepo.save(p);
        localPersons.put(key, saved);
        return saved;
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static String nullToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    @Transactional
    public DTO.ApartmentDto createApartment(Long buildingId, CreateApartmentRequest req) {
        Building b = buildingRepo.findById(buildingId)
                .orElseThrow(() -> new EntityNotFoundException("Building not found"));

        Apartment a = new Apartment();
        a.setBuilding(b);
        a.setApartmentNumber(req.apartmentNumber);
        a.setActive(req.active != null ? req.active : Boolean.TRUE);
        a.setPriority(req.priority);
        a.setSequence(req.sequence);
        a.setMbr(req.mbr);
        a.setHepMBR(req.hepMBR);
        a.setHepMBRWater(req.hepMBRWater);
        a.setMjernoMjesto(req.mjernoMjesto);
        a.setDecimalno(req.decimalno);

        if (req.person != null) {
            Person p;
            if (req.person.id != null) {
                p = personRepo.findById(req.person.id)
                        .orElseThrow(() -> new EntityNotFoundException("Person not found"));
            } else if (req.person.firstName != null && !req.person.firstName.trim().isEmpty()) {
                p = new Person();
                p.setFirstName(req.person.firstName.trim());
                p.setLastName(req.person.lastName);
                p.setContact(req.person.contact);
                p = personRepo.save(p);
            } else {
                p = null;
            }
            a.setPerson(p);
        }

        Apartment saved = apartmentRepo.save(a);
        return DTOMapper.toDto(saved);
    }


}
