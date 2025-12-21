package com.example.energy.service;

import com.example.energy.model.Apartment;
import com.example.energy.model.Person;
import com.example.energy.repository.ApartmentRepository;
import com.example.energy.repository.PersonRepository;
import com.example.energy.viewmodel.ApartmentViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApartmentService {
    private final ApartmentRepository apartmentRepository;
    private final PersonRepository personRepository;

    public ApartmentService(ApartmentRepository apartmentRepository, PersonRepository personRepository) {
        this.apartmentRepository = apartmentRepository;
        this.personRepository = personRepository;
    }
    public ApartmentViewModel getApartmentByPersonName(String name) {
        Person person = personRepository.findByFirstNameIgnoreCase(name).orElse(null);
        Apartment apartment = apartmentRepository.findByPerson(person);
        return ApartmentViewModel.createViewModel(apartment);
    }
    public List<Apartment> findAll() {
        return apartmentRepository.findAll();
    }
}
