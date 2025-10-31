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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

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


    private final MeterRepository meterRepository;
    private final ApartmentService apartmentService;
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final MeasurementRepository measurementRepository;
    private final CityRepository cityRepository;
    private final PersonRepository personRepository;


    public byte[] exportDataForBuilding(ExportDataViewModel dataViewModel) throws IOException {
        for (String buildingId : dataViewModel.getLists()) {
            Optional<Building> building = buildingRepository.findByCodeIgnoreCase(buildingId);
            if (building.isPresent()) {
                Building build = building.get();
                List<MeasurementRow> rows = build.getApartments().stream()
                        // Ignore apartments without hepMBR
                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
                        // Sort by sequence
                        .sorted(Comparator.comparingInt(apartment -> apartment.getSequence() != null ? apartment.getSequence() : 0))
                        // Map each apartment to a MeasurementRow with summed value
                        .map(apartment -> {
                            double sum = apartment.getMeters().stream()
                                    .filter(meter -> Boolean.TRUE.equals(meter.getActive()))
                                    .flatMap(meter -> meter.getMeasurements().stream())
                                    .filter(measurement -> measurement.getValue() != null)
                                    .mapToDouble(measurement -> measurement.getValue())
                                    .sum();

                            return new MeasurementRow(apartment.getHepMBR(), (int) sum);
                        })
                        .toList();

                return writeToExcel(rows,"/resources/excel");
            }
        }

        return new byte[0];
    }


    public byte[] writeToExcel(List<MeasurementRow> rows, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Measurements");

            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Hep Mbr");
            header.createCell(1).setCellValue("Measurement Value");

            // Write data
            int rowIndex = 1;
            for (MeasurementRow rowData : rows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowData.getHepMbr());
                row.createCell(1).setCellValue(rowData.getValue());
            }

            // Autosize columns
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                workbook.write(bos);
                return bos.toByteArray(); // <-- return the bytes
            }
        }
    }

}
