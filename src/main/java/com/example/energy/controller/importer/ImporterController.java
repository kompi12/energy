package com.example.energy.controller.importer;

import com.example.energy.model.City;
import com.example.energy.model.Meter;
import com.example.energy.repository.CityRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.importer.ImporterService;
import com.example.energy.viewmodel.ExportDataViewModel;
import com.example.energy.viewmodel.MissingMetersDataViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/import")
public class ImporterController {

    private final CityRepository cityRepository;

    public ImporterController(ImporterService importerService, CityRepository cityRepository) {
        this.importerService = importerService;
        this.cityRepository = cityRepository;
    }


    private final ImporterService importerService;

    @PostMapping
    @RequestMapping("/importExcelData")
    public EnergyResponse importData(@RequestParam("file") MultipartFile file) {
        try {
            importerService.importInitialData(file);
            return EnergyResponse.success(EnergyResponse.success("File uploaded successfully", null));

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


    @PostMapping
    @RequestMapping("/importSequence")
    public EnergyResponse importSequence(@RequestParam("file") MultipartFile file) {
        try {
            importerService.importSequence(file);
            return EnergyResponse.success(EnergyResponse.success("File uploaded successfully", null));

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


    @PostMapping
    @RequestMapping("/importDataForMonth")
    public EnergyResponse importDataForMonth(@RequestParam("file") MultipartFile file) {
        try {
            importerService.importDataForMonth(file);
            return EnergyResponse.success(EnergyResponse.success("File uploaded successfully", null));

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


    @PostMapping("/importMetersMissing")
    public EnergyResponse exportBuildings(@RequestBody MissingMetersDataViewModel exportData) {
        try {

            importerService.importMissingMeters(exportData);
            return EnergyResponse.success(EnergyResponse.success("Meters added successfully", null));
        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }

    @PostMapping("/importMetersMissingSjenjak")
    public EnergyResponse importBuildingSjenjak(@RequestParam("file") MultipartFile file) {
        try {

            importerService.importNewMeters(file);
            return EnergyResponse.success(EnergyResponse.success("Meters added successfully", null));
        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


    @PostMapping
    @RequestMapping("/importXML")
    public EnergyResponse importXML(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return EnergyResponse.error(500, "No file added");
            }
            importerService.importXML(file);
            return EnergyResponse.success(EnergyResponse.success("File uploaded successfully", null));

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


}
