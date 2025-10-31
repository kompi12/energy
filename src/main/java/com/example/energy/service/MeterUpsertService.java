package com.example.energy.service;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeterUpsertService {

    private final MeterRepository meterRepository;
    private final ApartmentRepository apartmentRepository;
    private final PersonRepository personRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingAddressRepository addressRepository;
    private final CityRepository cityRepository;

    /**
     * Find-or-create everything needed and return the Meter.
     */
    @Transactional
    public Meter findOrCreateMeter(
            String mbr,
            String addressLine,
            String cityName,
            String meterCode,
            String power,
            String buildingCode,
            String personFirstName,
            String siemensSN
    ) {
        // Normalize
        String m = t(mbr);
        String addr = t(addressLine);
        String cName = t(cityName);
        String mCode = t(meterCode);
        String pwr = t(power);
        String bCode = t(buildingCode);
        String pName = t(personFirstName);
        String siemens = t(siemensSN);

        // Fast path: existing meter
        Optional<Meter> existingMeter = meterRepository.findByCode(mCode);
        if(!siemens.isEmpty()) {
            if(existingMeter.isPresent()) {
                Optional<Meter> siemenMeter = meterRepository.findByCode(siemens);
                if(siemenMeter.isPresent()) {
                    return siemenMeter.get();
                }
                Meter existing = existingMeter.get();
                existing.setActive(false);

                Optional<Apartment> apartment = apartmentRepository.findByMbr(m);
                Meter newMeter = new Meter();
                newMeter.setCode(siemensSN);
                newMeter.setActive(true);
                newMeter.setPower(power);
                newMeter.setApartment(existing.getApartment());
                meterRepository.save(newMeter);
                meterRepository.save(existing);
            }

        }
        if (existingMeter.isPresent() ) return existingMeter.get();


        // City
        City city = cityRepository.findByNameIgnoreCase(cName)
                .orElseGet(() -> {
                    City created = new City();
                    created.setName(cName);
                    return cityRepository.save(created);
                });

        // Building by code (+ city). If no code provided, generate one.
        Building building = (bCode != null
                ? buildingRepository.findByCodeIgnoreCaseAndCity_NameIgnoreCase(bCode, city.getName())
                : Optional.<Building>empty())
                .orElseGet(() -> {
                    Building b = new Building();
                    b.setCity(city);
                    b.setCode(bCode != null ? bCode : "BLDG-" + UUID.randomUUID());
                    return buildingRepository.save(b);
                });

        // Address row (optional but recommended)
        if (addr != null) {
            addressRepository.findByBuilding_IdAndAddressLineIgnoreCase(building.getId(), addr)
                    .orElseGet(() -> {
                        BuildingAddress ba = new BuildingAddress();
                        ba.setBuilding(building);
                        ba.setAddressLine(addr);
                        ba.setCity(city.getName());
                        ba.setCountry(city.getCountry());
                        return addressRepository.save(ba);
                    });
        }

        // Apartment by MBR
        Apartment apartment = apartmentRepository.findByMbr(m)
                .orElseGet(() -> {
                    Apartment a = new Apartment();
                    a.setMbr(m);
                    a.setBuilding(building);
                    return apartmentRepository.save(a);
                });

        // Person link (optional)
        if (pName != null) {
            Person person = personRepository.findByFirstNameIgnoreCase(pName)
                    .orElseGet(() -> {
                        Person p = new Person();
                        p.setFirstName(pName);
                        return personRepository.save(p);
                    });
            person.addApartment(apartment);
            personRepository.save(person);
        }

        // Create meter
        Meter meter = new Meter();
        meter.setApartment(apartment);
        meter.setCode(mCode);
        meter.setPower(pwr);
        return meterRepository.save(meter);
    }

    private static String t(String s) {
        if (s == null) return null;
        String x = s.trim();
        return x.isEmpty() ? null : x;
    }
}
