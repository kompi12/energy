package com.example.energy.repository;

import com.example.energy.model.Apartment;
import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    Optional<Apartment> findByMbr(String mbr);
}