package com.example.energy.controller.importer;

import com.example.energy.repository.CityRepository;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.importer.ImporterWaterService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/importWater")
public class ImporterWaterController {


    public ImporterWaterController(ImporterWaterService importerService, CityRepository cityRepository) {
        this.importerWaterService = importerService;
    }


    private final ImporterWaterService importerWaterService;


    @PostMapping
    @RequestMapping("/importTechem")
    public EnergyResponse importTechem(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return EnergyResponse.error(500, "No file added");
            }
            importerWaterService.importDataForMonth(file);
            return EnergyResponse.success("File uploaded successfully", null);

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }

    @PostMapping
    @RequestMapping("/importCodes")
    public EnergyResponse importCodes(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return EnergyResponse.error(500, "No file added");
            }
            importerWaterService.importCodes(file);
            return EnergyResponse.success("File uploaded successfully", null);

        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }


}
