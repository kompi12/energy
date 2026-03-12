package com.example.energy.controller.importer;

import com.example.energy.repository.CityRepository;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.importer.ImporterService;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.MissingMetersDataViewModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImporterController {


    public ImporterController(ImporterService importerService, CityRepository cityRepository) {
        this.importerService = importerService;
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
    @RequestMapping("/checkBuildingsTechem")
    public EnergyResponse<List<String>> checkBuildingsTechem(@RequestParam("file") MultipartFile file) {
        try {
           List<String> missingBuildings =  importerService.checkBuildingsTechem(file);
            return EnergyResponse.success("File uploaded successfully", missingBuildings);

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

    @PostMapping("/importMetersMissinJD7")
    public EnergyResponse importBuildingSjenjakJD7(@RequestParam("file") MultipartFile file) {
        try {

            importerService.importNewMetersJD7(file);
            return EnergyResponse.success("Meters added successfully", null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500, "error");

        }
    }



    @PostMapping(value = "/print-fabnr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EnergyResponse<List<String>> printFabnrFromXml(
            @RequestParam("file") MultipartFile file) {

        List<String> fabnrs = importerService.extractFabnrForDate(file);

        return EnergyResponse.success("Success",fabnrs);
    }

    @PostMapping(value = "/importTechem", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnergyResponse<DTO.ImportResult>> importTechem(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(EnergyResponse.error(400, "No file added"));
            }
            DTO.ImportResult result = importerService.importDataForMonth(file);
            return ResponseEntity.ok(EnergyResponse.success("Techem import završen.", result));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(EnergyResponse.error(500, ex.getMessage()));
        }
    }

    @PostMapping(value = "/importXML", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EnergyResponse<DTO.ImportResult>> importXML(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(EnergyResponse.error(400, "No file added"));
            }
            DTO.ImportResult result = importerService.importXMLFast(file);
            return ResponseEntity.ok(EnergyResponse.success("XML import završen.", result));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(EnergyResponse.error(500, ex.getMessage()));
        }
    }




}
