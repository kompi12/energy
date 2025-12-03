package com.example.energy.service;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class MeasurementUpsertService {

    private final MeterRepository meterRepository;
    private final ApartmentRepository apartmentRepository;
    private final PersonRepository personRepository;
    private final BuildingRepository buildingRepository;
    private final BuildingAddressRepository addressRepository;
    private final CityRepository cityRepository;
    private final MeasurementRepository measurementRepository;
    Logger logger = Logger.getLogger(MeasurementUpsertService.class.getName());

    /**
     * Find-or-create everything needed and return the Meter.
     */
    @Transactional
    public Measurement createMeasurement(
            String dateXml,
            String valueXml,
            String pNumberXml
    ) {
        // Normalize
        String date = t(dateXml);
        String value = t(valueXml);
        String pNumber = t(pNumberXml);


//        Optional<Building> building = buildingRepository.findById(403L);
//        List<Long> listOfId = new ArrayList<>();
//        listOfId.add(412L);
//        listOfId.add(413L);
//
//        listOfId.add(414L);
//        listOfId.add(418L);
//        listOfId.add(453L);
//        listOfId.add(458L);
//
//        listOfId.add(510L);

        Optional<Meter> meter = meterRepository.findByCodeAndActiveTrue(pNumber);


       // List<Building> buildings = buildingRepository.findAllById(listOfId);
       // Optional<Meter> meter = meterRepository.findByBuildingsAndMeter(listOfId, Long.valueOf(pNumber));
        //Optional<Meter> meter = meterRepository.findByBuildingAndMeter(building.get().getId(), Long.valueOf(pNumber));

        if(meter.isEmpty()) {
            logger.info("Meter not found  {}" + pNumber);
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        LocalDate dateLocal = dateTime.toLocalDate();
        Optional<Measurement> measurement = measurementRepository.findByMeterAndMeasureDate(meter.get(),dateLocal);
        if(measurement.isPresent()) {
            logger.info("Measurement is present" + pNumber);
            return null;
        }


        Measurement newMeasurement = new Measurement();
        newMeasurement.setMeasureDate(dateLocal);
        newMeasurement.setValue(Integer.parseInt(value));
        newMeasurement.setMeter(meter.get());
        return  measurementRepository.save(newMeasurement);
    }

    private static String t(String s) {
        if (s == null) return null;
        String x = s.trim();
        return x.isEmpty() ? null : x;
    }
}
