package com.example.energy.controller.importer;

import com.example.energy.repository.CityRepository;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.importer.ImporterWaterService;
import com.example.energy.viewmodel.dto.DTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/importWater")
public class ImporterWaterController {


    public ImporterWaterController(ImporterWaterService importerService, CityRepository cityRepository) {
        this.importerWaterService = importerService;
    }


    private final ImporterWaterService importerWaterService;


    @PostMapping(value = "/importTechem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnergyResponse<DTO.ImportResult>> importTechem(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(EnergyResponse.error(400, "No file added"));
            }
            DTO.ImportResult result = importerWaterService.importWaterDataForMonth(file);
            return ResponseEntity.ok(EnergyResponse.success("Techem import završen.", result));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(EnergyResponse.error(500, ex.getMessage()));
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
