package com.example.energy.service.export;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentService;
import com.example.energy.viewmodel.ExportDataViewModel;
import com.example.energy.viewmodel.MeasurementRow;
import com.example.energy.viewmodel.MeasurementRowWithPerson;
import com.example.energy.viewmodel.MeasurementRowWithPersonForMeter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.util.*;
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

//                List<MeasurementRow> rows = building.getApartments().stream()
//                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
//                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
//                        .sorted(Comparator.comparingInt(a -> a.getSequence() != null ? a.getSequence() : 0))
//                        .map(apartment -> {
//                            double sum = apartment.getMeters().stream()
//                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
//                                    .flatMap(m -> m.getMeasurements().stream())
//                                    .filter(meas -> meas.getValue() != null)
//                                    .mapToDouble(Measurement::getValue)
//                                    .sum();
//
//                            return new MeasurementRow(apartment.getHepMBR(), (int) sum);
//                        })
//                        .collect(Collectors.toList());

                YearMonth thisMonth = YearMonth.of(dataViewModel.getYear(), dataViewModel.getMonth());
                YearMonth lastMonth = thisMonth.minusMonths(1);


                List<MeasurementRow> rowsDiff = building.getApartments().stream()
                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
                        .sorted(Comparator.comparingInt(a -> a.getSequence() != null ? a.getSequence() : 0))
                        .map(apartment -> {

                            double thisMonthSum = apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .flatMap(m -> m.getMeasurements().stream())
                                    .filter(meas -> meas.getValue() != null)
                                    .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(thisMonth))
                                    .mapToDouble(Measurement::getValue)
                                    .sum();

                            double lastMonthSum = apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .flatMap(m -> m.getMeasurements().stream())
                                    .filter(meas -> meas.getValue() != null)
                                    .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(lastMonth))
                                    .mapToDouble(Measurement::getValue)
                                    .sum();

                            int diff = (int) Math.max(thisMonthSum - lastMonthSum, 0);

                            return new MeasurementRow(apartment.getHepMBR(), diff);
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
                byte[] csvBytes = writeToCsv(rowsDiff, building.getCode());

                // Add CSV to ZIP
                String fileName = building.getName() + dataViewModel.getDate() + ".csv";
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

    public byte[] exportDataForBuildingsVinkovci(ExportDataViewModel dataViewModel) throws IOException {
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            for (String buildingId : dataViewModel.getLists()) {
                Optional<Building> buildingOpt = buildingRepository.findByCodeIgnoreCase(buildingId);

                if (buildingOpt.isEmpty()) {
                    logger.warn("Building not found for id: {}", buildingId);
                    continue;
                }

                Building building = buildingOpt.get();

                YearMonth thisMonth = YearMonth.of(dataViewModel.getYear(), dataViewModel.getMonth());
                YearMonth lastMonth = thisMonth.minusMonths(1);


                List<MeasurementRow> rowsDiff = building.getApartments().stream()
                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
                        .sorted(Comparator.comparingInt(a -> a.getSequence() != null ? a.getSequence() : 0))
                        .map(apartment -> {

                            double thisMonthSum = apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .flatMap(m -> m.getMeasurements().stream())
                                    .filter(meas -> meas.getValue() != null)
                                    .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(thisMonth))
                                    .mapToDouble(Measurement::getValue)
                                    .sum();

                            double lastMonthSum = apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .flatMap(m -> m.getMeasurements().stream())
                                    .filter(meas -> meas.getValue() != null)
                                    .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(lastMonth))
                                    .mapToDouble(Measurement::getValue)
                                    .sum();

                            int diff = (int) Math.max(thisMonthSum - lastMonthSum, 0);

                            return new MeasurementRow(apartment.getHepMBR(), diff, apartment.getPerson().getFirstName(), apartment.getBuilding()
                                    .getAddresses()
                                    .stream()
                                    .map(BuildingAddress::getAddressLine)
                                    .collect(Collectors.joining(", ")));
                        })
                        .collect(Collectors.toList());

                byte[] csvBytes = writeToCsvVinkovci(rowsDiff);

                String fileName = building.getName() + dataViewModel.getDate() + ".csv";
                ZipEntry entry = new ZipEntry(fileName);
                zipOut.putNextEntry(entry);
                zipOut.write(csvBytes);
                zipOut.closeEntry();

                storeCsvLocally(fileName, csvBytes);
            }
        }

        return zipBos.toByteArray();
    }

    public byte[] exportDataForBuildingsVinkovciOneSheet(ExportDataViewModel dataViewModel) throws IOException {

        List<MeasurementRow> allRows = new ArrayList<>();

        // Collect all data for all buildings
        for (String buildingId : dataViewModel.getLists()) {
            Optional<Building> buildingOpt = buildingRepository.findByCodeIgnoreCase(buildingId);

            if (buildingOpt.isEmpty()) {
                logger.warn("Building not found for id: {}", buildingId);
                continue;
            }

            Building building = buildingOpt.get();
            YearMonth thisMonth = YearMonth.of(dataViewModel.getYear(), dataViewModel.getMonth());
            YearMonth lastMonth = thisMonth.minusMonths(1);

            List<MeasurementRow> rows = building.getApartments().stream()
                    .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
                    .map(apartment -> {
                        double thisMonthSum = apartment.getMeters().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                .flatMap(m -> m.getMeasurements().stream())
                                .filter(meas -> meas.getValue() != null)
                                .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(thisMonth))
                                .mapToDouble(Measurement::getValue)
                                .sum();

                        double lastMonthSum = apartment.getMeters().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                .flatMap(m -> m.getMeasurements().stream())
                                .filter(meas -> meas.getValue() != null)
                                .filter(meas -> YearMonth.from(meas.getMeasureDate()).equals(lastMonth))
                                .mapToDouble(Measurement::getValue)
                                .sum();

                        int diff = (int) Math.max(thisMonthSum - lastMonthSum, 0);

                        String address = apartment.getBuilding().getAddresses().stream()
                                .map(BuildingAddress::getAddressLine)
                                .collect(Collectors.joining(", "));

                        return new MeasurementRow(
                                apartment.getMbr(),
                                diff,
                                apartment.getPerson().getFirstName(),
                                address
                        );
                    })
                    .sorted(Comparator.comparing(MeasurementRow::getHepMbr))
                    .toList();

            allRows.addAll(rows);
        }

        // Write all rows into a single CSV
        byte[] csvBytes = writeToCsvVinkovci(allRows);
        String csvFileName = "ALL_BUILDINGS_" + dataViewModel.getDate() + ".csv";

        // Create ZIP containing the CSV
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            ZipEntry entry = new ZipEntry(csvFileName);
            zipOut.putNextEntry(entry);
            zipOut.write(csvBytes);
            zipOut.closeEntry();
        }

        // Optionally store locally
        storeCsvLocally(csvFileName, csvBytes);

        return zipBos.toByteArray();
    }





    public byte[] exportDataForBuildingsWithPerson(ExportDataViewModel dataViewModel) throws IOException {
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            for (String buildingId : dataViewModel.getLists()) {
                Optional<Building> buildingOpt = buildingRepository.findByCodeIgnoreCase(buildingId);

                if (buildingOpt.isEmpty()) {
                    logger.warn("Building not found for id: {}", buildingId);
                    continue;
                }

                Building building = buildingOpt.get();

                // Collect rows: each apartment's total meter value + person name
                List<MeasurementRowWithPerson> rows = building.getApartments().stream()
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

                            String personName = apartment.getPerson() != null
                                    ? apartment.getPerson().getFirstName()
                                    : "";

                            return new MeasurementRowWithPerson(apartment.getHepMBR(), (int) sum, personName);
                        })
                        .collect(Collectors.toList());

                // Create CSV
                byte[] csvBytes = writeToCsvWithPerson(rows);

                // Add CSV to ZIP
                String fileName = building.getName() + "_" + dataViewModel.getDate() + ".csv";
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

    public byte[] exportDataForBuildingsWithPersonForMeters(ExportDataViewModel dataViewModel) throws IOException {
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            for (String buildingId : dataViewModel.getLists()) {
                Optional<Building> buildingOpt = buildingRepository.findByCodeIgnoreCase(buildingId);

                if (buildingOpt.isEmpty()) {
                    logger.warn("Building not found for id: {}", buildingId);
                    continue;
                }

                Building building = buildingOpt.get();

                // Collect rows: each apartment's total meter value + person name
                List<MeasurementRowWithPersonForMeter> rows = building.getApartments().stream()
                        .filter(apartment -> Boolean.TRUE.equals(apartment.getActive()))
//                        .filter(apartment -> apartment.getHepMBR() != null && !apartment.getHepMBR().isBlank())
                        .sorted(Comparator.comparingInt(a -> a.getSequence() != null ? a.getSequence() : 0))
                        .flatMap(apartment -> apartment.getMeters().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                .map(meter -> {
                                    double meterValue = meter.getMeasurements().stream()
                                            .filter(meas -> meas.getValue() != null)
                                            .mapToDouble(Measurement::getValue)
                                            .sum(); // sum per meter – not per apartment

                                    String personName = apartment.getPerson() != null
                                            ? apartment.getPerson().getFirstName()
                                            : "";

                                    return new MeasurementRowWithPersonForMeter(
                                            apartment.getHepMBR(),
                                            (int) meterValue,
                                            meter.getCode(),
                                            personName,
                                            apartment.getMbr()
                                    );
                                })
                        )
                        .collect(Collectors.toList());


                // Create CSV
                byte[] csvBytes = writeToXlsx(rows);
                //writeToCsvWithPersonForMeter(rows);

                // Add CSV to ZIP
                String fileName = building.getName() + "_" + dataViewModel.getDate() + ".csv";
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


    private byte[] writeToCsvWithPerson(List<MeasurementRowWithPerson> rows) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Index;Apartment;Person;Value\n");

        int index = 1;
        for (MeasurementRowWithPerson row : rows) {
            sb.append(index++)
                    .append(';')
                    .append(row.getHepMbr())
                    .append(';')
                    .append(row.getPersonName())
                    .append(';')
                    .append(row.getValue())
                    .append('\n');
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
        //return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] writeToCsvWithPersonForMeter(List<MeasurementRowWithPersonForMeter> rows) throws IOException {
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (MeasurementRowWithPersonForMeter row : rows) {
            sb.append(index++)
                    .append(';')
                    .append(row.getHepMbr())
                    .append(';')
                    .append(row.getMbr())
                    .append(';')
                    .append(row.getPersonName())
                    .append(';')
                    .append(row.getMeterCode())
                    .append(';')
                    .append(row.getValue())
                    .append('\n');
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
        //return sb.toString().getBytes(StandardCharsets.UTF_8);
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

    private byte[] writeToCsvVinkovci(List<MeasurementRow> rows) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Data rows
        int index = 1;
        for (MeasurementRow row : rows) {
            sb.append(index++)
                    .append(';')
                    .append(row.getAddress())
                    .append(';')
                    .append(row.getPersonName())
                    .append(';')
                    .append(row.getHepMbr())
                    .append(';')
                    .append(row.getValue())
                    .append('\n');
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
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

    private byte[] writeToXlsx(List<MeasurementRowWithPersonForMeter> rows) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Export");

        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        header.createCell(0).setCellValue("Index");
        header.createCell(1).setCellValue("Apartment");
        // header.createCell(2).setCellValue("MBR");
        header.createCell(3).setCellValue("Person");
        header.createCell(4).setCellValue("Meter");
        header.createCell(5).setCellValue("Value");

        int index = 1;
        for (MeasurementRowWithPersonForMeter r : rows) {
            Row xlsRow = sheet.createRow(rowNum++);
            xlsRow.createCell(0).setCellValue(index++);
            xlsRow.createCell(1).setCellValue(r.getHepMbr());
            //xlsRow.createCell(2).setCellValue(r.getMbr());
            xlsRow.createCell(3).setCellValue(r.getPersonName());
            xlsRow.createCell(4).setCellValue(r.getMeterCode());
            xlsRow.createCell(5).setCellValue(r.getValue());
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return bos.toByteArray();
    }

}
