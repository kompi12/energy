package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
}

