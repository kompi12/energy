package com.example.energy.repository;

import com.example.energy.model.Apartment;
import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
}
