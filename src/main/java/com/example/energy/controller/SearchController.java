package com.example.energy.controller;

import com.example.energy.service.SearchService;
import com.example.energy.viewmodel.dto.ApartmentSearchResultDto;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.DeviceSearchResultDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = {"http://localhost:5173"})
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/apartments")
    public List<ApartmentSearchResultDto> apartments(@RequestParam String q) {
        return searchService.apartments(q);
    }

    @GetMapping("/water-meters")
    public List<DeviceSearchResultDto> waterMeters(@RequestParam String q) {
        return searchService.waterMeters(q);
    }

    @GetMapping("/meters")
    public List<DeviceSearchResultDto> meters(@RequestParam String q) {
        return searchService.meters(q);
    }
}
