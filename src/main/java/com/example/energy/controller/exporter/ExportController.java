package com.example.energy.controller.exporter;

import com.example.energy.response.EnergyResponse;
import com.example.energy.service.export.ExportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/export")
public class ExportController {

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }


    private  final ExportService exportService;

    @PostMapping
    @RequestMapping("/importExcelData")
    public EnergyResponse importData(@RequestParam("file") MultipartFile file) {
        try {
            //exportService.importInitalData(file);
            return EnergyResponse.success(EnergyResponse.success("File uploaded successfully", null));

        } catch(Exception ex) {
            ex.printStackTrace();
            return EnergyResponse.error(500,"error");

        }
    }






}
