package com.example.energy.service.importer;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentUpsertService;
import com.example.energy.service.MeasurementService;
import com.example.energy.service.MeasurementUpsertService;
import com.example.energy.service.WaterMeterService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Double.parseDouble;


@Service
public class ImporterWaterService {

    private static final Logger log = LoggerFactory.getLogger(ImporterWaterService.class);

    private final ApartmentUpsertService apartmentUpsertService;
    private final MeasurementRepository measurementRepository;
    private final ApartmentRepository apartmentRepository;
    private final MeasurementService measurementService;
    private final MeasurementUpsertService measurementUpsertService;
    private final BuildingRepository buildingRepository;
    private final WaterMeterRepository waterMeterRepository;
    private final WaterMeterService waterMeterService;

    public ImporterWaterService(
            ApartmentUpsertService apartmentUpsertService,
            WaterMeterRepository waterMeterRepository,
            MeasurementRepository measurementRepository,
            ApartmentRepository apartmentRepository,
            MeasurementService measurementService,
            MeasurementUpsertService measurementUpsertService,

            BuildingRepository buildingRepository, WaterMeterService waterMeterService) {
        this.apartmentUpsertService = apartmentUpsertService;
        this.waterMeterRepository = waterMeterRepository;
        this.measurementRepository = measurementRepository;
        this.apartmentRepository = apartmentRepository;
        this.measurementService = measurementService;
        this.measurementUpsertService = measurementUpsertService;
        this.buildingRepository = buildingRepository;
        this.waterMeterService = waterMeterService;
    }
    /**
     * Imports *monthly* measurements from the first sheet.
     * Creates a Measurement if waterMeter exists; logs missing waterMeters.
     */
    @Transactional
    public void importDataForMonth(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            int inserted = 0, skipped = 0, missingwaterMeter = 0, duplicates = 0;

            // 1️⃣ Collect all waterMeter codes from the Excel
            Set<String> waterMeterCodes = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                String waterMeterCode = fmt.formatCellValue(r.getCell(6)).trim();
                if (!waterMeterCode.isEmpty()) waterMeterCodes.add(waterMeterCode);
            }
           // List<waterMeter> activewaterMeters =waterMeterRepository.findByCodeInAndActiveTrueAndApartmentActiveTrue(waterMeterCodes,true,true);
            List<WaterMeter> activewaterMeters = waterMeterRepository.findByCodeInAndActiveTrue(waterMeterCodes);
            Map<String, WaterMeter> waterMeterMap = activewaterMeters.stream()
                    .collect(Collectors.toMap(WaterMeter::getCode, m -> m));

            // 3️⃣ Preload existing measurements for these waterMeters (current month or all)
            List<WaterMeter> waterMeters = new ArrayList<>(waterMeterMap.values());
            Map<String, Set<LocalDate>> existingBywaterMeter = new HashMap<>();
            measurementRepository.findAllByWaterMeterIn(waterMeters).forEach(m -> {
                existingBywaterMeter
                        .computeIfAbsent(m.getWaterMeter().getCode(), k -> new HashSet<>())
                        .add(m.getMeasureDate());
            });


            List<Measurement> buffer = new ArrayList<>();
            final int BATCH_SIZE = 500;

            // 4️⃣ Iterate rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String waterMeterCode = fmt.formatCellValue(r.getCell(6)).trim();
                String measurementValue = fmt.formatCellValue(r.getCell(8)).trim();
                if (waterMeterCode.isEmpty() || measurementValue.isEmpty()) { skipped++; continue; }

                WaterMeter waterMeter = waterMeterMap.get(waterMeterCode);
                if (waterMeter == null) {
                    String person = fmt.formatCellValue(r.getCell(2)).trim();
                    log.info("Missing or inactive water waterMeter for row {}: code='{}', person='{}'", i, waterMeterCode, person);
                    missingwaterMeter++;
                    continue;
                }

                LocalDate date = parseLocalDate(r.getCell(7), fmt);
                if (date == null) { skipped++; continue; }

                // 🧠 Check if measurement already exists
                Set<LocalDate> existingDates = existingBywaterMeter.computeIfAbsent(waterMeterCode, k -> new HashSet<>());
                if (existingDates.contains(date)) {
                    duplicates++;
                    continue;
                }

                Double value = parseDouble(measurementValue);
                if (value == null) value = (double) 0;

                Measurement m = new Measurement();
                m.setWaterMeter(waterMeter);
                m.setMeasureDate(date);
                m.setValue(value);
                m.setCreatedAt(Instant.now());
                m.setCreatedBy("Monthly Import " + YearMonth.now());

                buffer.add(m);
                inserted++;
                existingDates.add(date); // ✅ now safe


                if (buffer.size() >= BATCH_SIZE) {
                    measurementRepository.saveAll(buffer);
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                measurementRepository.saveAll(buffer);
            }

            log.info("Monthly import done: inserted={}, duplicates={}, missingwaterMeter={}, skipped={}",
                    inserted, duplicates, missingwaterMeter, skipped);

        } catch (Exception e) {
            log.error("Monthly import failed: {}", e.getMessage(), e);
            throw new RuntimeException("Monthly import failed", e);
        }
    }


    @Transactional
    public void importCodes(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();
            int inserted = 0, skipped = 0, missing = 0;

            List<Apartment> buffer = new ArrayList<>();
            final int BATCH_SIZE = 500;

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String sifraMjerMjesta = fmt.formatCellValue(r.getCell(0)).trim();
                String mbr = fmt.formatCellValue(r.getCell(1)).trim();
                String hep_mbr = fmt.formatCellValue(r.getCell(2)).trim();
                String decimalno = fmt.formatCellValue(r.getCell(3)).trim();

                Apartment apartment = apartmentRepository.findByMbr(mbr).orElse(null);
                Apartment apartmentNew = apartmentRepository.findByMjernoMjesto(sifraMjerMjesta).orElse(null);

                // validation
                if (apartment == null || apartmentNew == null
                        || apartmentNew.getWaterMeters() == null
                        || apartmentNew.getWaterMeters().isEmpty()) {

                    missing++;
                    log.info("Missing or inactive apartment for row {}: mbr={}, mjerno={}", i, mbr, sifraMjerMjesta);
                    continue;  // IMPORTANT!
                }

                // update values
                apartment.getWaterMeters().clear();

                apartmentNew.getWaterMeters().forEach((waterMeter -> {
                    waterMeter.setApartment(apartment);
                    waterMeterRepository.save(waterMeter);
                }));
                apartment.setMjernoMjesto(apartmentNew.getMjernoMjesto());
                apartment.setHepMBRWater(hep_mbr);
                if(!decimalno.isEmpty()) {
                    apartment.setDecimalno(parseDouble(decimalno));
                }
                apartmentNew.setActive(false);
                apartmentNew.setMjernoMjesto(null);

                buffer.add(apartment);
                buffer.add(apartmentNew);
                inserted++;

                if (buffer.size() >= BATCH_SIZE) {
                    apartmentRepository.saveAll(buffer);
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                apartmentRepository.saveAll(buffer);
            }

            log.info("Monthly import done: inserted={}, missing={}", inserted, missing);

        } catch (Exception e) {
            log.error("Monthly import failed", e);
            throw new RuntimeException("Monthly import failed", e);
        }
    }



    private static String s(DataFormatter fmt, Cell cell) {
        if (cell == null) return null;
        String val = fmt.formatCellValue(cell);
        if (val == null) return null;
        val = val.trim();
        return val.isEmpty() ? null : val;
    }

    private static Integer parseInteger(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Supports ISO strings (yyyy-MM-dd) or Excel numeric date cells.
     */
    private static LocalDate parseLocalDate(Cell dateCell, DataFormatter fmt) {
        if (dateCell == null) return null;

        // If it’s numeric and is a date-formatted cell -> convert directly
        if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
            return dateCell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        // Else try formatted text (ISO string)
        String text = fmt.formatCellValue(dateCell);
        if (text == null || text.isBlank()) return null;

        try {
            return LocalDate.parse(text.trim()); // expects yyyy-MM-dd
        } catch (Exception ignored) {
            return null;
        }
    }






    private String safeGetText(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl == null || nl.getLength() == 0 || nl.item(0) == null) return "";
        String txt = nl.item(0).getTextContent();
        return txt == null ? "" : txt.trim();
    }

    private static String parseToIso(String input) {
        if (input == null || input.isBlank()) return null;

        // Try: "dd.MM.yy HH:mm:ss"
        DateTimeFormatter dtFormatter1 = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss", Locale.ENGLISH);
        // Try: "dd.MM.yy"
        DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("dd.MM.yy", Locale.ENGLISH);

        try {
            if (input.contains(" ")) {
                LocalDateTime ldt = LocalDateTime.parse(input, dtFormatter1);
                return ldt.toString(); // ISO_LOCAL_DATE_TIME
            } else {
                LocalDate ld = LocalDate.parse(input, dFormatter);
                return ld.toString(); // ISO_LOCAL_DATE
            }
        } catch (DateTimeParseException ex) {
            // Try a slightly different pattern (maybe 4-digit year or different separators)
            try {
                DateTimeFormatter dtFormatter2 = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);
                if (input.contains(" ")) {
                    LocalDateTime ldt = LocalDateTime.parse(input, dtFormatter2);
                    return ldt.toString();
                }
            } catch (DateTimeParseException e2) {
                // As a last resort, return the raw input (or null) so the service can decide
                return input;
            }
        }
        return input;
    }


}
