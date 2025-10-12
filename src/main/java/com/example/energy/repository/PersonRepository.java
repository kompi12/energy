package com.example.energy.repository;

import com.example.energy.model.Energy;
import com.example.energy.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
}
