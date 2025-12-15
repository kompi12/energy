package com.example.energy.controller.exporter;

import com.example.energy.service.export.ExportService;
import com.example.energy.viewmodel.ExportDataViewModel;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/buildings")
    public ResponseEntity<byte[]> exportBuildings(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildings(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/buildingsJD7")
    public ResponseEntity<byte[]> exportBuildingsJD7(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsJD7(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/buildingsVinkovci")
    public ResponseEntity<byte[]> exportBuildingsVinkovci(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsVinkovciOneSheet(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/buildingsP")
    public ResponseEntity<byte[]> exportPerosn(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsWithPerson(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/buildingsPersonByMeter")
    public ResponseEntity<byte[]> exportPersonByMeter(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsWithPersonForMeters(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/buildingsPersonKumulativno")
    public ResponseEntity<byte[]> exportPersonKumulativno(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsWithPersonKumulativno(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/buildingsPersonKumulativnoMeter")
    public ResponseEntity<byte[]> exportDataForBuildingsWithPersonKumulativnoByMeters(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] zipResponse = exportService.exportDataForBuildingsWithPersonKumulativnoByMeters(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=buildings_export.zip")
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .body(zipResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
