package com.example.energy.service;

import com.example.energy.model.Apartment;
import com.example.energy.model.Person;
import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.PersonRepository;
import com.example.energy.viewmodel.ApartmentViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;

    public ApartmentService(ApartmentRepository apartmentRepository, PersonRepository personRepository) {
        this.apartmentRepository = apartmentRepository;
    }
    public List<ApartmentViewModel> getApartmentByPersonName(String name) {
        List<ApartmentViewModel> apartmentViewModels = new ArrayList<>();
        List<Apartment> apartment = apartmentRepository.findApartmentsByPersonFirstNameLike(name);
        apartment.forEach(a ->apartmentViewModels.add(ApartmentViewModel.createViewModel(a)));
        return apartmentViewModels;
    }
    public List<Apartment> findAll() {
        return apartmentRepository.findAll();
    }
}
