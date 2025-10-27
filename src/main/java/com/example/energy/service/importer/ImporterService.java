package com.example.energy.service.importer;

import com.example.energy.model.Measurement;
import com.example.energy.model.Meter;

import com.example.energy.repository.MeasurementRepository;
import com.example.energy.repository.MeterRepository;
import com.example.energy.service.MeterUpsertService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Importer for initial master data and monthly measurements from Excel.
 *
 * Expected columns (by your current sheets):
 *   Sheet0 / Sheet3 (initial users+meters):
 *     [1]=sifraZgrade(buildingCode) [2]=city [3]=address [4]=mbr [5]=personName
 *     [6]=meterCode [7]=power [8]=value (optional)
 *
 *   Sheet2 / Sheet4 (audit of missing users):
 *     sheet2: [5]=personName [6]=mbr [7]=hep_mbr
 *     sheet4: [5]=personName [4]=mbr [7]=hep_mbr
 *
 *   Monthly measurements (importDataForMonth):
 *     [0]=sifraZgrade [1]=address [2]=personName [5]=meterCode
 *     [7]=date (ISO yyyy-MM-dd or Excel date) [8]=value (int)
 */
@Service
@RequiredArgsConstructor
public class ImporterService {

    private static final Logger log = LoggerFactory.getLogger(ImporterService.class);

    private final MeterUpsertService meterUpsertService;
    private final MeterRepository meterRepository;
    private final MeasurementRepository measurementRepository;

    // ---------- Public API ----------

    /**
     * Imports *monthly* measurements from the first sheet.
     * Creates a Measurement if meter exists; logs missing meters.
     */
    @Transactional
    public void importDataForMonth(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            DataFormatter fmt = new DataFormatter();

            int inserted = 0, skipped = 0, missingMeter = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String meterCode = fmt.formatCellValue(r.getCell(5)).trim();
                if (meterCode.isEmpty()) { skipped++; continue; }

                Optional<Meter> meterOpt = meterRepository.findByCode(meterCode);
                if (meterOpt.isEmpty()) {
                    // log who it was (if present) to help reconcile
                    String person = fmt.formatCellValue(r.getCell(2)).trim();
                    log.info("Missing meter for row {}: code='{}' person='{}'", i, meterCode, person);
                    missingMeter++;
                    continue;
                }

                Meter meter = meterOpt.get();

                Integer value = parseInteger(fmt.formatCellValue(r.getCell(8)));
                if (value == null) value = 0;

                LocalDate date = parseLocalDate(r.getCell(7), fmt);
                if (date == null) {
                    log.warn("Row {} skipped: invalid/empty date for meter {}", i, meterCode);
                    skipped++;
                    continue;
                }

                Measurement m = new Measurement();
                m.setMeter(meter);
                m.setMeasureDate(date);
                m.setValue(value);
                m.setCreatedAt(Instant.now());
                m.setCreatedBy("Excel Monthly Import");

                measurementRepository.save(m);
                inserted++;

                // simple batching every 200 rows
                if ((inserted % 200) == 0) {
                    measurementRepository.flush();
                }
            }

            log.info("Monthly import done: inserted={}, missingMeter={}, skipped={}", inserted, missingMeter, skipped);
        } catch (Exception e) {
            log.error("Monthly import failed: {}", e.getMessage(), e);
            throw new RuntimeException("Monthly import failed", e);
        }
    }

    /**
     * Imports *initial* master data from multiple sheets.
     * Uses find-or-create flow so it’s idempotent.
     */
    public void importInitialData(MultipartFile file) {
        try (InputStream in = file.getInputStream(); Workbook wb = new XSSFWorkbook(in)) {
            DataFormatter fmt = new DataFormatter();

            // ---- Sheet 0 ----
       //     importInitialSheet(wb.getSheetAt(0), fmt, /*logLabel*/"sheet0");

            // ---- Sheet 3 ----
            if (wb.getNumberOfSheets() > 2) {
                importInitialSheet(wb.getSheetAt(2), fmt, "sheet2");
            }
//            if (wb.getNumberOfSheets() > 2) {
//                importInitialSheet(wb.getSheetAt(2), fmt, "sheet2");
//            }
//
//            // ---- Sheet 2: audit missing users ----
//            if (wb.getNumberOfSheets() > 1) {
//                auditMissingUsersBySheet2(wb.getSheetAt(1), fmt);
//            }
//
//            // ---- Sheet 4: audit missing users (alt layout) ----
//            if (wb.getNumberOfSheets() > 3) {
//                auditMissingUsersBySheet4(wb.getSheetAt(3), fmt);
//            }

        } catch (Exception e) {
            log.error("Initial import failed: {}", e.getMessage(), e);
            throw new RuntimeException("Initial import failed", e);
        }
    }

    // ---------- Internal helpers ----------

    /** Initial sheet layout: uses indices you showed in your original code. */
    private void importInitialSheet(Sheet sheet, DataFormatter fmt, String label) {
        if (sheet == null) return;

        int createdMeters = 0, existingMeters = 0, rows = 0;

        for (int i = 1500; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;
            rows++;

            String buildingCode = s(fmt, r.getCell(1)); // sifraZgrade
            String city         = s(fmt, r.getCell(2));
            String address      = s(fmt, r.getCell(3));
            String mbr          = s(fmt, r.getCell(4));
            String personName   = s(fmt, r.getCell(5));
            String meterCode    = s(fmt, r.getCell(6));
            String power        = s(fmt, r.getCell(7));

            if (meterCode == null || mbr == null || city == null) {
                log.debug("[{}] Row {} skipped: required data missing (meterCode/mbr/city)", label, i);
                continue;
            }

            Optional<Meter> existing = meterRepository.findByCode(meterCode);
            if (existing.isPresent()) {
                existingMeters++;
                continue;
            }

            // Find-or-create full graph; address will be attached to the building (if provided)
            Meter created = meterUpsertService.findOrCreateMeter(
                    mbr, address, city, meterCode, power, buildingCode, personName
            );

            if (created != null) {
                createdMeters++;
                if ((createdMeters % 200) == 0) {
                    meterRepository.flush();
                }
            }
        }

        log.info("Initial import [{}]: rows={}, createdMeters={}, existingMeters={}",
                label, rows, createdMeters, existingMeters);
    }

    /** Audit sheet2: logs persons whose apartments (by MBR) are missing. */
    private void auditMissingUsersBySheet2(Sheet sheet, DataFormatter fmt) {
        if (sheet == null) return;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;

            String personName = s(fmt, r.getCell(5));
            String mbr        = s(fmt, r.getCell(6));
            // String hepMbr  = s(fmt, r.getCell(7)); // optional

            // If you kept ApartmentRepository.findByMbr, you can call it here if desired
            // Optional<Apartment> a = apartmentRepository.findByMbr(mbr);
            // if (a.isEmpty()) log.info("User doesn't exist (sheet2): {}", personName);

            // Since we’ve centralized creation in MeterUpsertService, this remains a simple audit log.
            log.debug("Audit (sheet2) row {} – person='{}', mbr='{}'", i, personName, mbr);
        }
    }

    /** Audit sheet4: similar, slightly different column positions. */
    private void auditMissingUsersBySheet4(Sheet sheet, DataFormatter fmt) {
        if (sheet == null) return;

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row r = sheet.getRow(i);
            if (r == null) continue;

            String personName = s(fmt, r.getCell(5));
            String mbr        = s(fmt, r.getCell(4));
            // String hepMbr  = s(fmt, r.getCell(7));

            log.debug("Audit (sheet4) row {} – person='{}', mbr='{}'", i, personName, mbr);
        }
    }

    // ---------- Parsing utilities ----------

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
}
