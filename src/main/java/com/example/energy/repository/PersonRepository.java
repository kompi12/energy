package com.example.energy.repository;

import com.example.energy.model.Energy;
import com.example.energy.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByFirstNameIgnoreCase(String firstName);
}