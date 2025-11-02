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
}
