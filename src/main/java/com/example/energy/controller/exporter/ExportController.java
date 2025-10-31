package com.example.energy.controller.exporter;

import com.example.energy.response.EnergyResponse;
import com.example.energy.service.export.ExportService;
import com.example.energy.viewmodel.ExportDataViewModel;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/export")
public class ExportController {

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }


    private  final ExportService exportService;

    @PostMapping("/exportData")
    public ResponseEntity<byte[]> importData(@RequestBody ExportDataViewModel exportData) {
        try {
            byte[] reponse = exportService.exportDataForBuilding(exportData);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=measurements.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(reponse);
        } catch(Exception ex) {
            ex.printStackTrace();
return ResponseEntity.badRequest().build();
        }
    }






}
