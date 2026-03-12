package com.example.energy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/debug/db")
    public String testDb() {
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return "DB OK: " + result;
    }
}