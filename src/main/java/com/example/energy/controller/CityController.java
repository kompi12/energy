package com.example.energy.controller;

import com.example.energy.model.City;
import com.example.energy.repository.CityRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@CrossOrigin(origins = {"http://localhost:5173"})
public class CityController {
    private final CityRepository repo;
    public CityController(CityRepository repo) { this.repo = repo; }

    @GetMapping
    public List<City> list() {
        return repo.findAll();
    }
}
