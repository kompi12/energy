package com.example.energy.service.export;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentService;
import com.example.energy.viewmodel.ExportDataViewModel;
import com.example.energy.viewmodel.MeasurementRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    private final MeterRepository meterRepository;
    private final ApartmentService apartmentService;
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final MeasurementRepository measurementRepository;
    private final CityRepository cityRepository;
    private final PersonRepository personRepository;

    public ExportService(
            MeterRepository meterRepository,
            ApartmentService apartmentService,
            ApartmentRepository apartmentRepository,
            BuildingRepository buildingRepository,
            MeasurementRepository measurementRepository,
            CityRepository cityRepository,
            PersonRepository personRepository
    ) {
        this.meterRepository = meterRepository;
        this.apartmentService = apartmentService;
        this.apartmentRepository = apartmentRepository;
        this.buildingRepository = buildingRepository;
        this.measurementRepository = measurementRepository;
        this.cityRepository = cityRepository;
        this.personRepository = personRepository;
    }

    /**
     * Generate a ZIP file containing Excel reports for multiple buildings.
     */
    public byte[] exportDataForBuildings(ExportDataViewModel dataViewModel) throws IOException {
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            for (String buildingId : dataViewModel.getLists()) {
                Optional<Building> buildingOpt = buildingRepository.findByCodeIgnoreCase(buildingId);

                if (buildingOpt.isEmpty()) {
                    logger.warn("Building not found for id: {}", buildingId);
                    continue;
                }

                Building building = buildingOpt.get();

                // Prepare measurement data
                List<MeasurementRow> rows = building.getApartments().stream()
                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
                        .sorted(Comparator.comparingInt(a -> a.getSequence() != null ? a.getSequence() : 0))
                        .map(apartment -> {
                            double sum = apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .flatMap(m -> m.getMeasurements().stream())
                                    .filter(meas -> meas.getValue() != null)
                                    .mapToDouble(Measurement::getValue)
                                    .sum();

                            return new MeasurementRow(apartment.getHepMBR(), (int) sum);
                        })
                        .collect(Collectors.toList());

                // Create Excel workbook for this building
                byte[] excelBytes = writeToExcel(rows, building.getCode());

                // Add Excel to ZIP
                String fileName = building.getCode() + "_measurements.xlsx";
                ZipEntry entry = new ZipEntry(fileName);
                zipOut.putNextEntry(entry);
                zipOut.write(excelBytes);
                zipOut.closeEntry();

                // Optional: store locally
                storeExcelLocally(fileName, excelBytes);
            }
        }

        return zipBos.toByteArray();
    }

    /**
     * Create Excel file in memory.
     */
    private byte[] writeToExcel(List<MeasurementRow> rows, String buildingCode) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Measurements");
            int rowIndex = 0;
            for (MeasurementRow rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowIndex);
                row.createCell(1).setCellValue(rowData.getHepMbr());
                row.createCell(2).setCellValue(rowData.getValue());
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);


            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * Optional: Save Excel to local disk.
     */
    private void storeExcelLocally(String fileName, byte[] data) {
        try {
            File outputDir = new File("exports");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(data);
            }

            logger.info("Saved Excel locally: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to store Excel locally", e);
        }
    }
}
