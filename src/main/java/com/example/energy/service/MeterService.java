package com.example.energy.service;

import com.example.energy.model.Apartment;
import com.example.energy.model.Meter;
import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.viewmodel.RequestBodyPersonMultipleViewModel;
import com.example.energy.viewmodel.RequestBodyPersonViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MeterService {


    private final MeterRepository meterRepository;
    private final ApartmentRepository apartmentRepository;

    public MeterService(MeterRepository meterRepository, ApartmentRepository apartmentRepository) {
        this.meterRepository = meterRepository;
        this.apartmentRepository = apartmentRepository;
    }

    public List<Meter> findAll() {
        return meterRepository.findAll();
    }

    public List<Meter> findAllForPersonName(String name) {
        return meterRepository.findByPersonFirstNameLikeIgnoreCase(name);
    }

    public Boolean deactivateMeter(String code) {
        Meter meter = meterRepository.findByCode(code).orElse(null);
        if (meter == null) return false;
        meter.setActive(false);
        meterRepository.save(meter);
        return true;
    }

    public Boolean deactivateMeters(RequestBodyPersonMultipleViewModel viewModel) {
        List<Meter> listOfMeters = new ArrayList<>();
        viewModel.getCode().forEach(code -> {
            Optional<Meter> meter = meterRepository.findByCodeAndApartment_Id(code, Long.valueOf(viewModel.getApartmentId()));
            meter.ifPresent(listOfMeters::add);
        });
        listOfMeters.forEach(meter -> {
            meter.setActive(false);
            meterRepository.save(meter);
        });
        return true;
    }

    public Boolean createMeter(RequestBodyPersonViewModel viewModel) {

        Apartment apartment = apartmentRepository.findById(Long.valueOf(viewModel.getApartmentId())).orElse(null);
        Meter meter = new Meter();
        meter.setCode(viewModel.getCode());
        meter.setApartment(apartment);
        meterRepository.save(meter);
        return true;
    }

    public Boolean createMeters(RequestBodyPersonMultipleViewModel viewModel) {

        Apartment apartment = apartmentRepository.findById(Long.valueOf(viewModel.getApartmentId())).orElse(null);
        viewModel.getCode().forEach(code -> {
            Meter meter = new Meter();
            meter.setCode(code);
            meter.setApartment(apartment);
            meterRepository.save(meter);
        });

        return true;
    }
}
