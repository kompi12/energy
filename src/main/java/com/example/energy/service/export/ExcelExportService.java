package com.example.energy.service.export;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.viewmodel.ExportQuery;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    private final BuildingRepository buildingRepo;
    private final ApartmentRepository apartmentRepo;
    private final MeterRepository meterRepo;
    private final MeasurementRepository measurementRepo;

    public ExcelExportService(BuildingRepository buildingRepo,
                              ApartmentRepository apartmentRepo,
                              MeterRepository meterRepo,
                              MeasurementRepository measurementRepo) {
        this.buildingRepo = buildingRepo;
        this.apartmentRepo = apartmentRepo;
        this.meterRepo = meterRepo;
        this.measurementRepo = measurementRepo;
    }

    public Building getBuilding(Long id) {
        return buildingRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Building not found: " + id));
    }

    public void exportBuildingOld(Long buildingId, ExportQuery q, OutputStream os) throws Exception {
        String mode = (q.mode() == null) ? "DETAIL" : q.mode().trim().toUpperCase();
        YearMonth fromYm = parseYmOrDefault(q.from(), YearMonth.now().minusMonths(3));
        YearMonth toYm = parseYmOrDefault(q.to(), YearMonth.now());

        if (toYm.isBefore(fromYm)) {
            YearMonth tmp = fromYm;
            fromYm = toYm;
            toYm = tmp;
        }

        List<YearMonth> months = monthsBetween(fromYm, toYm);
        LocalDate from = months.get(0).atDay(1);
        LocalDate to = months.get(months.size() - 1).atEndOfMonth();

        Building building = getBuilding(buildingId);

        // ✅ sve apartmane odjednom
        List<Apartment> apartments = apartmentRepo.findByBuilding_Id(buildingId);
        Map<Long, Apartment> aptById = apartments.stream()
                .collect(Collectors.toMap(Apartment::getId, a -> a));

        // ✅ sve metere odjednom (bez N+1)
        List<Meter> meters = meterRepo.findByApartment_Building_Id(buildingId);

        // map meterId -> apartment
        Map<Long, Apartment> aptByMeterId = new HashMap<>();
        for (Meter m : meters) {
            Apartment a = (m.getApartment() != null) ? aptById.get(m.getApartment().getId()) : null;
            if (a != null) aptByMeterId.put(m.getId(), a);
        }

        // ako nema metera u zgradi, ipak generiraj file s headerima
        List<Long> meterIds = meters.stream().map(Meter::getId).toList();

        // ✅ 2 upita ukupno za measurements (period + lastBefore)
        Map<Long, Double> prevByMeter = new HashMap<>();
        Map<Long, Map<YearMonth, Double>> endByMeter = new HashMap<>();

        if (!meterIds.isEmpty()) {
            List<Measurement> lastBefore = measurementRepo.findLastBeforeForMeters(meterIds, from);
            for (Measurement m : lastBefore) {
                prevByMeter.put(m.getMeter().getId(), m.getValue());
            }

            List<Measurement> inPeriod = measurementRepo.findAllForMetersInPeriod(meterIds, from, to);
            for (Measurement meas : inPeriod) {
                long mid = meas.getMeter().getId();
                YearMonth ym = YearMonth.from(meas.getMeasureDate());
                endByMeter.computeIfAbsent(mid, k -> new HashMap<>()).put(ym, meas.getValue());
            }
        }

        // ✅ streaming workbook (brže + manje RAM-a)
        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            wb.setCompressTempFiles(true);

            CellStyle header = createHeaderStyle(wb);
            CellStyle mono = createMonoStyle(wb);
            CellStyle num = createNumberStyle(wb);
            CellStyle intStyle = createIntStyle(wb);

            if ("SUMMARY".equals(mode)) {
                Sheet sh = wb.createSheet("Stanovi");
                writeSummarySheetFast(sh, header, mono, num, intStyle, building, apartments, meters, months, prevByMeter, endByMeter);
                // ✅ nema autosize (ili samo par prvih kolona)
                autoSizeSafe(sh, 10);            } else {
                Sheet sh = wb.createSheet("Razdjelnici");
                writeDetailSheetFast(sh, header, mono, num, intStyle, building, meters, aptByMeterId, months, prevByMeter, endByMeter);
                autoSizeSafe(sh, 10);            }

            wb.write(os);
            wb.dispose();
        }
    }

    public void exportBuilding(Long buildingId, ExportQuery q, OutputStream os) throws Exception {
        String mode = (q.mode() == null) ? "DETAIL" : q.mode().trim().toUpperCase();
        YearMonth fromYm = parseYmOrDefault(q.from(), YearMonth.now().minusMonths(3));
        YearMonth toYm = parseYmOrDefault(q.to(), YearMonth.now());

        if (toYm.isBefore(fromYm)) {
            YearMonth tmp = fromYm;
            fromYm = toYm;
            toYm = tmp;
        }

        List<YearMonth> months = monthsBetween(fromYm, toYm);
        LocalDate from = months.get(0).atDay(1);
        LocalDate to = months.get(months.size() - 1).atEndOfMonth();

        Building building = getBuilding(buildingId);

        // ✅ sve apartmane odjednom
        List<Apartment> apartments = apartmentRepo.findByBuilding_Id(buildingId);
        Map<Long, Apartment> aptById = apartments.stream()
                .collect(Collectors.toMap(Apartment::getId, a -> a));

        // ✅ sve metere odjednom (bez N+1)
        List<Meter> meters = meterRepo.findByApartment_Building_Id(buildingId);

        // ✅ samo apartmani koji imaju barem 1 Meter (water metere ignoriramo potpuno)
        Set<Long> aptIdsWithMeter = meters.stream()
                .map(Meter::getApartment)
                .filter(Objects::nonNull)
                .map(Apartment::getId)
                .collect(Collectors.toSet());

        List<Apartment> apartmentsForExport = apartments.stream()
                .filter(a -> aptIdsWithMeter.contains(a.getId()))
                .toList();

        // map meterId -> apartment
        Map<Long, Apartment> aptByMeterId = new HashMap<>();
        for (Meter m : meters) {
            Apartment a = (m.getApartment() != null) ? aptById.get(m.getApartment().getId()) : null;
            if (a != null) aptByMeterId.put(m.getId(), a);
        }

        // ako nema metera u zgradi, ipak generiraj file s headerima
        List<Long> meterIds = meters.stream().map(Meter::getId).toList();

        // ✅ 2 upita ukupno za measurements (period + lastBefore)
        Map<Long, Double> prevByMeter = new HashMap<>();
        Map<Long, Map<YearMonth, Double>> endByMeter = new HashMap<>();

        if (!meterIds.isEmpty()) {
            List<Measurement> lastBefore = measurementRepo.findLastBeforeForMeters(meterIds, from);
            for (Measurement m : lastBefore) {
                prevByMeter.put(m.getMeter().getId(), m.getValue());
            }

            List<Measurement> inPeriod = measurementRepo.findAllForMetersInPeriod(meterIds, from, to);
            for (Measurement meas : inPeriod) {
                long mid = meas.getMeter().getId();
                YearMonth ym = YearMonth.from(meas.getMeasureDate());
                endByMeter.computeIfAbsent(mid, k -> new HashMap<>()).put(ym, meas.getValue());
            }
        }

        // ✅ streaming workbook (brže + manje RAM-a)
        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            wb.setCompressTempFiles(true);

            CellStyle header = createHeaderStyle(wb);
            CellStyle mono = createMonoStyle(wb);
            CellStyle num = createNumberStyle(wb);
            CellStyle intStyle = createIntStyle(wb);

            if ("SUMMARY".equals(mode)) {
                Sheet sh = wb.createSheet("Stanovi");

                // ✅ export samo za stanove s barem jednim Meter-om
                writeSummarySheetFast(
                        sh, header, mono, num, intStyle,
                        building, apartmentsForExport, meters, months,
                        prevByMeter, endByMeter
                );

                autoSizeSafe(sh, 10);
            } else {
                Sheet sh = wb.createSheet("Razdjelnici");

                writeDetailSheetFast(
                        sh, header, mono, num, intStyle,
                        building, meters, aptByMeterId, months,
                        prevByMeter, endByMeter
                );

                autoSizeSafe(sh, 10);
            }

            wb.write(os);
            wb.dispose();
        }
    }

    // -------------------- SUMMARY FAST
    private void writeSummarySheetFast(Sheet sh,
                                       CellStyle header, CellStyle mono, CellStyle num, CellStyle intStyle,
                                       Building b,
                                       List<Apartment> apartments,
                                       List<Meter> allMeters,
                                       List<YearMonth> months,
                                       Map<Long, Double> prevByMeter,
                                       Map<Long, Map<YearMonth, Double>> endByMeter) {

        int r = 0;

        Row title = sh.createRow(r++);
        title.createCell(0).setCellValue("Zgrada: " + nz(b.getName()) + " " + nz(b.getName()));

        r++; // empty

        Row h = sh.createRow(r++);
        int c = 0;

        c = setHeader(h, c, header, "Grad");
        c = setHeader(h, c, header, "Zgrada ");
        c = setHeader(h, c, header, "Osoba");
        c = setHeader(h, c, header, "Hep MBR");
        c = setHeader(h, c, header, "Broj razdjelnika");

        for (YearMonth ym : months) {
            String m = hrMonthNominative(ym.getMonthValue());
            c = setHeader(h, c, header, m + " - stanje");
            c = setHeader(h, c, header, m + " - potrošnja");
        }

        c = setHeader(h, c, header, "Ukupno potrošnja (period)");

        // group meters by apartmentId
        Map<Long, List<Meter>> metersByApartment = new HashMap<>();
        for (Meter m : allMeters) {
            if (m.getApartment() == null) continue;
            Long aid = m.getApartment().getId();
            metersByApartment.computeIfAbsent(aid, k -> new ArrayList<>()).add(m);
        }

        for (Apartment a : apartments) {
            List<Meter> meters = metersByApartment.getOrDefault(a.getId(), List.of());

            Map<YearMonth, Double> endSum = new LinkedHashMap<>();
            Map<YearMonth, Double> consSum = new LinkedHashMap<>();
            for (YearMonth ym : months) {
                endSum.put(ym, 0.0);
                consSum.put(ym, 0.0);
            }

            // sumiraj po meterima
            for (Meter m : meters) {
                Long mid = m.getId();
                Map<YearMonth, Double> endMap = endByMeter.getOrDefault(mid, Collections.emptyMap());
                Double prev = prevByMeter.get(mid);

                for (YearMonth ym : months) {
                    Double end = endMap.get(ym);
                    if (end != null) endSum.put(ym, endSum.get(ym) + end);

                    double cons = safeDiff(prev, end);
                    consSum.put(ym, consSum.get(ym) + cons);

                    if (end != null) prev = end;
                }
            }

            double total = consSum.values().stream().mapToDouble(Double::doubleValue).sum();

            Row row = sh.createRow(r++);
            int cc = 0;

            String city = (b.getCity() != null) ? nz(b.getCity().getName()) : "";
            cc = setText(row, cc, city, null);
            cc = setText(row, cc, nz(b.getCode()), mono);
            cc = setText(row, cc, personLabel(a.getPerson()), null);
            cc = setText(row, cc, pickHepMbr(a), mono);

            Cell mCount = row.createCell(cc++);
            mCount.setCellValue(meters.size());
            mCount.setCellStyle(intStyle);

            for (YearMonth ym : months) {
                Cell st = row.createCell(cc++);
                st.setCellValue(endSum.get(ym));
                st.setCellStyle(num);

                Cell co = row.createCell(cc++);
                co.setCellValue(consSum.get(ym));
                co.setCellStyle(num);
            }

            Cell tCell = row.createCell(cc++);
            tCell.setCellValue(total);
            tCell.setCellStyle(num);
        }

        sh.createFreezePane(0, 3);
        sh.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                2, Math.max(2, r - 1),
                0, Math.max(0, c - 1)
        ));
    }

    // -------------------- DETAIL FAST
    private void writeDetailSheetFast(Sheet sh,
                                      CellStyle header, CellStyle mono, CellStyle num, CellStyle intStyle,
                                      Building b,
                                      List<Meter> meters,
                                      Map<Long, Apartment> aptByMeterId,
                                      List<YearMonth> months,
                                      Map<Long, Double> prevByMeter,
                                      Map<Long, Map<YearMonth, Double>> endByMeter) {

        int r = 0;

        Row title = sh.createRow(r++);
        title.createCell(0).setCellValue("Zgrada: " + nz(b.getCode()) + " " + nz(b.getName()));

        r++; // empty

        Row h = sh.createRow(r++);
        int c = 0;

        c = setHeader(h, c, header, "Grad");
        c = setHeader(h, c, header, "Zgrada (kod)");
        c = setHeader(h, c, header, "Hep MBR");
        c = setHeader(h, c, header, "Naziv osobe");
        c = setHeader(h, c, header, "Serijski broj");

        for (YearMonth ym : months) {
            String monthName = hrMonthNominative(ym.getMonthValue());
            c = setHeader(h, c, header, monthName + " - stanje");
            c = setHeader(h, c, header, monthName + " - potrošnja");
        }

        for (Meter m : meters) {
            Long mid = m.getId();
            Map<YearMonth, Double> endMap = endByMeter.getOrDefault(mid, Collections.emptyMap());
            boolean any = endMap.values().stream().anyMatch(Objects::nonNull);
            if (!any) continue;

            Apartment a = aptByMeterId.get(mid);

            Row row = sh.createRow(r++);
            int cc = 0;

            String city = (b.getCity() != null) ? nz(b.getCity().getName()) : "";
            cc = setText(row, cc, city, null);
            cc = setText(row, cc, nz(b.getCode()), mono);
            cc = setText(row, cc, (a == null) ? "" : pickHepMbr(a), mono);
            cc = setText(row, cc, (a == null) ? "" : personLabel(a.getPerson()), null);
            cc = setText(row, cc, nz(m.getCode()), mono);

            Double prev = prevByMeter.get(mid);

            for (YearMonth ym : months) {
                Double end = endMap.get(ym);

                Cell st = row.createCell(cc++);
                if (end == null) {
                    st.setCellValue(0);
                    st.setCellStyle(intStyle);
                } else {
                    st.setCellValue(end);
                    st.setCellStyle(num);
                }

                double cons = safeDiff(prev, end);
                Cell co = row.createCell(cc++);
                co.setCellValue(cons);
                co.setCellStyle(num);

                if (end != null) prev = end;
            }
        }

        sh.createFreezePane(0, 3);
        sh.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(
                2, Math.max(2, r - 1),
                0, Math.max(0, c - 1)
        ));
    }

    // -------------------- helpers

    private static YearMonth parseYmOrDefault(String s, YearMonth def) {
        try {
            if (s == null || s.isBlank()) return def;
            return YearMonth.parse(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static List<YearMonth> monthsBetween(YearMonth from, YearMonth to) {
        List<YearMonth> out = new ArrayList<>();
        YearMonth cur = from;
        while (!cur.isAfter(to)) {
            out.add(cur);
            cur = cur.plusMonths(1);
        }
        return out;
    }

    private static String hrMonthNominative(int month) {
        return switch (month) {
            case 1 -> "siječanj";
            case 2 -> "veljača";
            case 3 -> "ožujak";
            case 4 -> "travanj";
            case 5 -> "svibanj";
            case 6 -> "lipanj";
            case 7 -> "srpanj";
            case 8 -> "kolovoz";
            case 9 -> "rujan";
            case 10 -> "listopad";
            case 11 -> "studeni";
            case 12 -> "prosinac";
            default -> "—";
        };
    }

    private static double safeDiff(Double prev, Double cur) {
        if (prev == null || cur == null) return 0.0;
        double d = cur - prev;
        return d < 0 ? 0.0 : d;
    }

    private static String pickHepMbr(Apartment a) {
        String x = nz(a.getHepMBR());
        if (!x.isBlank()) return x;
        return nz(a.getHepMBRWater());
    }

    private static String personLabel(Person p) {
        if (p == null) return "";
        String last = (p.getLastName() == null) ? "" : p.getLastName();
        return (nz(p.getFirstName()) + " " + last).trim();
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static int setHeader(Row row, int col, CellStyle style, String text) {
        Cell cell = row.createCell(col);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        return col + 1;
    }

    private static int setText(Row row, int col, String text, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(text == null ? "" : text);
        if (style != null) cell.setCellStyle(style);
        return col + 1;
    }

    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    private static CellStyle createMonoStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setFontName("Consolas");
        s.setFont(f);
        return s;
    }

    private static CellStyle createNumberStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("0.00"));
        return s;
    }

    private static CellStyle createIntStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("0"));
        return s;
    }

    private static void autoSizeSafe(Sheet sh, int maxColumns) {
        int lastColumn = sh.getRow(2) != null
                ? sh.getRow(2).getLastCellNum()
                : maxColumns;

        int limit = Math.min(lastColumn, maxColumns);

        for (int i = 0; i < limit; i++) {
            try {
                sh.autoSizeColumn(i);
            } catch (Exception ignored) {
            }
        }
    }

}
