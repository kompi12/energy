package com.example.energy.service.export;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentService;
import com.example.energy.viewmodel.ExportDataViewModel;
import com.example.energy.viewmodel.MeasurementRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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


//                List<MeasurementRow> rows = building.getApartments().stream()
//                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
//                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
//                        .sorted(Comparator.comparing(
//                                Apartment::getSequence,
//                                Comparator.nullsLast(Comparator.naturalOrder())
//                        ))
//                        .map(apartment -> {
//                            double sum = 0.0;
//
//                            if (apartment.getMeters() != null && !apartment.getMeters().isEmpty()) {
//                                sum = apartment.getMeters().stream()
//                                        .filter(m -> Boolean.TRUE.equals(m.getActive()))
//                                        .flatMap(m -> m.getMeasurements() != null ? m.getMeasurements().stream() : Stream.<Measurement>empty())
//                                        .filter(meas -> meas.getValue() != null)
//                                        .mapToDouble(Measurement::getValue)
//                                        .sum();
//                            }
//
//                            return new MeasurementRow(apartment.getHepMBR(), (int) sum);
//                        })
//                        .collect(Collectors.toList());


                // Create CSV file for this building
                byte[] csvBytes = writeToCsv(rows, building.getCode());

                // Add CSV to ZIP
                String fileName = building.getName() + dataViewModel.getDate() +".csv";
                ZipEntry entry = new ZipEntry(fileName);
                zipOut.putNextEntry(entry);
                zipOut.write(csvBytes);
                zipOut.closeEntry();

                // Optional: store locally
                storeCsvLocally(fileName, csvBytes);
            }
        }

        return zipBos.toByteArray();
    }
    private byte[] writeToCsv(List<MeasurementRow> rows, String buildingCode) {
        StringBuilder sb = new StringBuilder();
        // Data rows
        int index = 1;
        for (MeasurementRow row : rows) {
            sb.append(index++)
                    .append(';')
                    .append(row.getHepMbr())
                    .append(';')
                    .append(row.getValue())
                    .append('\n');
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }


    private void storeCsvLocally(String fileName, byte[] data) {
        try {
            File outputDir = new File("exports");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(data);
            }

            logger.info("Saved CSV locally: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to store CSV locally", e);
        }
    }
}
