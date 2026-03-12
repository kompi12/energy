package com.example.energy.service;

import com.example.energy.model.Apartment;
import com.example.energy.model.WaterMeter;
import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.WaterMeterRepository;
import com.example.energy.viewmodel.dto.WaterMeterPersonViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonMultipleViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonViewModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WaterMeterService {


    private final WaterMeterRepository WaterMeterRepository;
    private final ApartmentRepository apartmentRepository;

    public WaterMeterService(WaterMeterRepository WaterMeterRepository, ApartmentRepository apartmentRepository) {
        this.WaterMeterRepository = WaterMeterRepository;
        this.apartmentRepository = apartmentRepository;
    }

    public List<WaterMeter> findAll() {
        return WaterMeterRepository.findAll();
    }

    public List<WaterMeter> findAllForPersonName(String name) {
        return WaterMeterRepository.findByPersonFirstNameLikeIgnoreCase(name);
    }

    public WaterMeterPersonViewModel findAllForPersonWaterMeter(String name) {
        WaterMeterPersonViewModel WaterMeterPersonViewModel = new WaterMeterPersonViewModel();

        List<WaterMeter> listOfWaterMetersForPerson = WaterMeterRepository.findByPersonFirstNameLikeIgnoreCase(name);

        List<String> active = listOfWaterMetersForPerson.stream()
                .filter(WaterMeter::getActive)
                .map(WaterMeter::getCode)
                .toList();

        List<String> deactive = listOfWaterMetersForPerson.stream()
                .filter(m -> !m.getActive())
                .map(WaterMeter::getCode)
                .toList();

        WaterMeterPersonViewModel.setActive(active);
        WaterMeterPersonViewModel.setDeactivated(deactive);
        return WaterMeterPersonViewModel;
    }

    public Boolean deactivateWaterMeter(String code) {
        WaterMeter WaterMeter = WaterMeterRepository.findByCode(code).orElse(null);
        if (WaterMeter == null) return false;
        WaterMeter.setActive(false);
        WaterMeterRepository.save(WaterMeter);
        return true;
    }

    public Boolean deactivateWaterMeters(RequestBodyPersonMultipleViewModel viewModel) {
        List<WaterMeter> listOfWaterMeters = new ArrayList<>();
        viewModel.getCode().forEach(code -> {
            Optional<WaterMeter> WaterMeter = WaterMeterRepository.findByCodeAndApartment_Id(code, Long.valueOf(viewModel.getApartmentId()));
            WaterMeter.ifPresent(listOfWaterMeters::add);
        });
        listOfWaterMeters.forEach(WaterMeter -> {
            WaterMeter.setActive(false);
            WaterMeterRepository.save(WaterMeter);
        });
        return true;
    }

    public Boolean createWaterMeter(RequestBodyPersonViewModel viewModel) {

        Apartment apartment = apartmentRepository.findById(Long.valueOf(viewModel.getApartmentId())).orElse(null);
        WaterMeter WaterMeter = new WaterMeter();
        WaterMeter.setCode(viewModel.getCode());
        WaterMeter.setApartment(apartment);
        WaterMeterRepository.save(WaterMeter);
        return true;
    }

    public Boolean createWaterMeters(RequestBodyPersonMultipleViewModel viewModel) {

        Apartment apartment = apartmentRepository.findById(Long.valueOf(viewModel.getApartmentId())).orElse(null);
        viewModel.getCode().forEach(code -> {
            WaterMeter WaterMeter = new WaterMeter();
            WaterMeter.setCode(code);
            WaterMeter.setApartment(apartment);
            WaterMeterRepository.save(WaterMeter);
        });

        return true;
    }
}
