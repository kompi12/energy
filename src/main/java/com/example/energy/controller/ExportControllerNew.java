package com.example.energy.controller;

import com.example.energy.model.Building;
import com.example.energy.service.export.ExcelExportService;
import com.example.energy.viewmodel.ExportQuery;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exports")
@CrossOrigin(origins = {"http://localhost:5173"})
public class ExportControllerNew {

    private final ExcelExportService excelExportService;

    public ExportControllerNew(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @GetMapping("/buildings/{buildingId}/apartments.xlsx")
    public void exportBuildingApartmentsXlsx(
            @PathVariable Long buildingId,
            ExportQuery q,
            HttpServletResponse res
    ) throws Exception {

        Building building = excelExportService.getBuilding(buildingId);

        String safeName = sanitizeFileName(
                nz(building.getCode()) + " - " + nz(building.getName())
        );

        res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", "attachment; filename=\"" + safeName + ".xlsx\"");

        excelExportService.exportBuilding(buildingId, q, res.getOutputStream());
    }

    private static String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "export";
        return name.replaceAll("[\\\\/:*?\"<>|]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
