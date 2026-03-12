package com.example.energy.service.importer;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentUpsertService;
import com.example.energy.service.MeasurementService;
import com.example.energy.service.MeasurementUpsertService;
import com.example.energy.service.WaterMeterService;
import com.example.energy.viewmodel.dto.DTO;
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
    public DTO.ImportResult importWaterDataForMonth(MultipartFile file) {

        int inserted = 0;
        int missingWaterMeter = 0;
        int skippedEmpty = 0;
        int skippedBadDate = 0;
        int skippedBadValue = 0;
        int duplicates = 0;

        final int BATCH_SIZE = 1000;
        final int MISSING_LOG_LIMIT = 500;
        final int WARNING_LIMIT = 1000;

        Set<String> missingWaterMeterCodes = new LinkedHashSet<>();
        List<String> warnings = new ArrayList<>();

        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {

            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            Set<String> waterMeterCodes = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String code = fmt.formatCellValue(r.getCell(6)).trim();
                if (!code.isEmpty()) {
                    waterMeterCodes.add(code);
                }
            }

            List<WaterMeter> activeWaterMeters =
                    waterMeterRepository.findByCodeInAndActiveTrue(waterMeterCodes);

            Map<String, WaterMeter> waterMeterMap = activeWaterMeters.stream()
                    .collect(Collectors.toMap(WaterMeter::getCode, m -> m));

            List<WaterMeter> waterMeters = new ArrayList<>(waterMeterMap.values());

            Map<String, Set<LocalDate>> existingByWaterMeter = new HashMap<>();

            if (!waterMeters.isEmpty()) {
                measurementRepository.findAllByWaterMeterIn(waterMeters)
                        .forEach(m -> {

                            if (m.getWaterMeter() == null) return;

                            existingByWaterMeter
                                    .computeIfAbsent(
                                            m.getWaterMeter().getCode(),
                                            k -> new HashSet<>()
                                    )
                                    .add(m.getMeasureDate());
                        });
            }

            List<Measurement> buffer = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                Row r = sheet.getRow(i);
                if (r == null) continue;

                String waterMeterCode = fmt.formatCellValue(r.getCell(6)).trim();
                String measurementValue = fmt.formatCellValue(r.getCell(8)).trim();

                if (waterMeterCode.isEmpty() || measurementValue.isEmpty()) {

                    skippedEmpty++;

                    if (warnings.size() < WARNING_LIMIT) {
                        warnings.add(
                                "Row " + i +
                                        " skipped: empty code/value (waterMeter=" +
                                        waterMeterCode +
                                        ", value=" +
                                        measurementValue +
                                        ")"
                        );
                    }

                    continue;
                }

                WaterMeter waterMeter = waterMeterMap.get(waterMeterCode);

                if (waterMeter == null) {

                    missingWaterMeter++;

                    if (warnings.size() < WARNING_LIMIT) {
                        warnings.add(
                                "Row " + i +
                                        " skipped: water meter NOT FOUND or INACTIVE (waterMeter=" +
                                        waterMeterCode +
                                        ")"
                        );
                    }

                    if (missingWaterMeterCodes.size() < MISSING_LOG_LIMIT) {
                        missingWaterMeterCodes.add(waterMeterCode);
                    }

                    continue;
                }

                String rawDate = fmt.formatCellValue(r.getCell(7)).trim();
                LocalDate date = parseLocalDate(r.getCell(7), fmt);

                if (date == null) {

                    skippedBadDate++;

                    if (warnings.size() < WARNING_LIMIT) {
                        warnings.add(
                                "Row " + i +
                                        " skipped: INVALID DATE (waterMeter=" +
                                        waterMeterCode +
                                        ", rawDate=" +
                                        rawDate +
                                        ")"
                        );
                    }

                    continue;
                }

                Set<LocalDate> existingDates =
                        existingByWaterMeter.computeIfAbsent(
                                waterMeterCode,
                                k -> new HashSet<>()
                        );

                if (existingDates.contains(date)) {

                    duplicates++;
                    

                    continue;
                }

                Double value = parseDouble(measurementValue);

                if (value == null) {

                    skippedBadValue++;

                    if (warnings.size() < WARNING_LIMIT) {
                        warnings.add(
                                "Row " + i +
                                        " skipped: INVALID VALUE (waterMeter=" +
                                        waterMeterCode +
                                        ", value=" +
                                        measurementValue +
                                        ")"
                        );
                    }

                    continue;
                }

                Measurement m = new Measurement();
                m.setWaterMeter(waterMeter);
                m.setMeasureDate(date);
                m.setValue(value);
                m.setCreatedAt(Instant.now());
                m.setCreatedBy("Monthly Import " + YearMonth.now());

                buffer.add(m);

                inserted++;
                existingDates.add(date);

                if (buffer.size() >= BATCH_SIZE) {
                    measurementRepository.saveAll(buffer);
                    buffer.clear();
                }
            }

            if (!buffer.isEmpty()) {
                measurementRepository.saveAll(buffer);
            }

            if (missingWaterMeter > 0) {

                if (!missingWaterMeterCodes.isEmpty()) {

                    log.warn(
                            "Missing water meters (not found or inactive). Count={}, showing up to {}: {}",
                            missingWaterMeter,
                            MISSING_LOG_LIMIT,
                            String.join(", ", missingWaterMeterCodes)
                    );

                    if (missingWaterMeterCodes.size() == MISSING_LOG_LIMIT) {
                        log.warn(
                                "Missing water meters list truncated at {} items.",
                                MISSING_LOG_LIMIT
                        );
                    }

                } else {
                    log.warn(
                            "Missing water meters (not found or inactive). Count={}",
                            missingWaterMeter
                    );
                }
            }

            if (warnings.size() == WARNING_LIMIT) {
                log.warn(
                        "Warnings truncated at {} rows.",
                        WARNING_LIMIT
                );
            }

            log.info(
                    "Imported monthly water data: inserted={}, duplicates={}, missingWaterMeter={}, skippedEmpty={}, skippedBadDate={}, skippedBadValue={}",
                    inserted,
                    duplicates,
                    missingWaterMeter,
                    skippedEmpty,
                    skippedBadDate,
                    skippedBadValue
            );

            int skipped =
                    skippedEmpty +
                            skippedBadDate +
                            skippedBadValue +
                            duplicates;

            return new DTO.ImportResult(
                    "TECHEM",
                    0,
                    inserted,
                    duplicates,
                    missingWaterMeter,
                    skipped,
                    warnings
            );

        } catch (Exception e) {

            log.error(
                    "Monthly water import failed: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Monthly water import failed: " + e.getMessage(),
                    e
            );
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
