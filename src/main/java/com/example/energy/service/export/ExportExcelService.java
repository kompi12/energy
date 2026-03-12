package com.example.energy.service.export;

import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentRowMapper;
import com.example.energy.service.ApartmentService;
import com.example.energy.service.helper.HelperService;
import com.example.energy.viewmodel.dto.ExportDataViewModel;
import com.example.energy.viewmodel.dto.MeasurementRowWithPersonDynamic;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExportExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    private final BuildingRepository buildingRepository;
    private final HelperService helperService;
    public ExportExcelService(
            BuildingRepository buildingRepository,
            HelperService helperService
    ) {
        this.buildingRepository = buildingRepository;
        this.helperService = helperService;
    }

    public <T> byte[] exportDataDynamic (
            ExportDataViewModel dataViewModel,
            ApartmentRowMapper<T> mapper,
            BiFunction<List<T>, List<YearMonth>, byte[]> writer
    ) throws IOException {
        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            for (String buildingId : dataViewModel.getLists()) {

                Optional<Building> buildingOpt =
                        buildingRepository.findByCodeIgnoreCase(buildingId);

                if (buildingOpt.isEmpty()) {
                    logger.warn("Building not found for id: {}", buildingId);
                    continue;
                }

                Building building = buildingOpt.get();
                List<YearMonth> months =
                        getMonthsFromOctober(dataViewModel.getYear(), dataViewModel.getMonth());

                List<T> rows = building.getApartments().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getActive()))
                        .sorted(Comparator.comparingInt(a ->
                                Optional.ofNullable(a.getSequence()).orElse(0)))
                        .flatMap(a -> {
                            try {
                                return mapper.map(a, months);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();

                byte[] fileBytes = writer.apply(rows, months);

                String fileName =
                        building.getName() + "_" + dataViewModel.getDate() + ".xlsx";

                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.write(fileBytes);
                zipOut.closeEntry();

                helperService.storeCsvLocally(fileName, fileBytes);
            }

        }
    return zipBos.toByteArray();


    }

    public <T> byte[] exportDataDynamicVinkovic(
            ExportDataViewModel dataViewModel,
            ApartmentRowMapper<T> mapper,
            BiFunction<List<T>, List<YearMonth>, byte[]> writer
    ) throws IOException {

        List<T> allRows = new ArrayList<>();

        List<YearMonth> months =
                getMonthsFromOctober(
                        dataViewModel.getYear(),
                        dataViewModel.getMonth()
                );

        // 🔹 skupljamo sve podatke
        for (String buildingId : dataViewModel.getLists()) {

            Optional<Building> buildingOpt =
                    buildingRepository.findByCodeIgnoreCase(buildingId);

            if (buildingOpt.isEmpty()) {
                logger.warn("Building not found for id: {}", buildingId);
                continue;
            }

            Building building = buildingOpt.get();

            building.getApartments().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getActive()))
                    .sorted(Comparator.comparingInt(a ->
                            Optional.ofNullable(a.getSequence()).orElse(Integer.MAX_VALUE)
                    ))
                    .flatMap(a -> {
                        try {
                            return mapper.map(a, months);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .forEach(allRows::add);
        }

        // 🔹 jedan jedini file
        byte[] fileBytes = writer.apply(allRows, months);

        String fileName =
                "VINKOVCI_" + dataViewModel.getDate() + ".xlsx";

        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();

        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(fileBytes);
            zipOut.closeEntry();
        }

        helperService.storeCsvLocally(fileName, fileBytes);

        return zipBos.toByteArray();
    }


    public <T> byte[] exportDataDynamicVinkovicGlobal(
            ExportDataViewModel dataViewModel,
            ApartmentRowMapper<T> mapper,
            BiFunction<List<T>, List<YearMonth>, byte[]> writer
    ) throws IOException {

        List<T> allRows = new ArrayList<>();

        List<YearMonth> months = getMonthsFromOctober(
                dataViewModel.getYear(),
                dataViewModel.getMonth()
        );

        // 1) Skupi SVE aktivne apartmane iz SVIH zgrada
        List<Apartment> allApartments = new ArrayList<>();

        for (String buildingId : dataViewModel.getLists()) {

            Optional<Building> buildingOpt =
                    buildingRepository.findByCodeIgnoreCase(buildingId);

            if (buildingOpt.isEmpty()) {
                logger.warn("Building not found for id: {}", buildingId);
                continue;
            }

            Building building = buildingOpt.get();

            building.getApartments().stream()
                    .filter(a -> Boolean.TRUE.equals(a.getActive()))
                    .forEach(allApartments::add);
        }

        // 2) GLOBALNO sortiranje po sequence (nulls last)
        allApartments.sort(
                Comparator.comparing(
                        Apartment::getSequence,
                        Comparator.nullsLast(Integer::compareTo)
                )
        );

        // 3) Mapiranje u redove po globalno sortiranom poretku
        for (Apartment a : allApartments) {
            try {
                mapper.map(a, months).forEach(allRows::add);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 4) Jedan jedini file (xlsx bytes)
        byte[] fileBytes = writer.apply(allRows, months);

        // 5) Isti naming + zip kao "obični"
        String fileName = "VINKOVCI_" + dataViewModel.getDate() + ".xlsx";

        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipBos)) {
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.write(fileBytes);
            zipOut.closeEntry();
        }

        // 6) Isti store kao "obični" (spremaš xlsx bytes, ne zip)
        helperService.storeCsvLocally(fileName, fileBytes);

        // 7) Vrati zip bytes (kao obični)
        return zipBos.toByteArray();
    }

    @SneakyThrows
    public byte[] exportByMeters(ExportDataViewModel vm) throws IOException {

        return exportDataDynamicVinkovic(
                vm,
                (apartment, months) ->
                        apartment.getMeters().stream()
                                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                .map(meter -> {

                                    Map<YearMonth, Integer> cumulative =
                                            sumMeasurementsByMonth(meter);

                                    Map<YearMonth, MonthValue> monthly =
                                            calculateMonthlyValues(cumulative, months);

                                    return new MeasurementRowWithPersonDynamic(
                                            apartment.getHepMBR(),
                                            apartment.getPerson() != null
                                                    ? apartment.getPerson().getFirstName()
                                                    : "",
                                            apartment.getBuilding().getCity().getName(),
                                            apartment.getBuilding().getAddresses().stream()
                                                    .map(BuildingAddress::getAddressLine)
                                                    .collect(Collectors.joining(",")),
                                            meter.getCode(),
                                            monthly
                                    );
                                }),
                this::writeToXlsxKumulativnoMeterDynamicSafe
        );
    }

    @SneakyThrows
    public byte[] exportByApartments(ExportDataViewModel vm) throws IOException {

        return exportDataDynamicVinkovic(
                vm,
                (apartment, months) -> {

                    Map<YearMonth, Integer> cumulative =
                            apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .map(this::sumMeasurementsByMonth)
                                    .flatMap(m -> m.entrySet().stream())
                                    .collect(Collectors.groupingBy(
                                            Map.Entry::getKey,
                                            LinkedHashMap::new,
                                            Collectors.summingInt(Map.Entry::getValue)
                                    ));

                    Map<YearMonth, MonthValue> monthly =
                            calculateMonthlyValues(cumulative, months);

                    return Stream.of(
                            new MeasurementRowWithPersonDynamic(
                                    apartment.getHepMBR(),
                                    apartment.getPerson() != null
                                            ? apartment.getPerson().getFirstName()
                                            : "",
                                    apartment.getBuilding().getCity().getName(),
                                    apartment.getBuilding().getAddresses().stream()
                                            .map(BuildingAddress::getAddressLine)
                                            .collect(Collectors.joining(",")),
                                    "APARTMENT",
                                    monthly
                            )
                    );
                },
                this::writeToXlsxKumulativnoMeterDynamicSafe
        );
    }

    @SneakyThrows
    public byte[] exportByApartmentsVinkovci(ExportDataViewModel vm) {

        return exportDataDynamicVinkovic(
                vm,

                (apartment, months) -> {

                    if (apartment.getSequence() == null ||
                            apartment.getMbr() == null) {
                        return Stream.empty();
                    }

                    // kumulativ po mjesecima (SVE mjere svih brojila)
                    Map<YearMonth, Integer> cumulative =
                            apartment.getMeters().stream()
                                    .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                    .map(this::sumMeasurementsByMonth)
                                    .flatMap(m -> m.entrySet().stream())
                                    .collect(Collectors.groupingBy(
                                            Map.Entry::getKey,
                                            LinkedHashMap::new,
                                            Collectors.summingInt(Map.Entry::getValue)
                                    ));

                    Map<YearMonth, MonthValue> monthly =
                            calculateMonthlyValues(cumulative, months);

                    return Stream.of(
                            new MeasurementRowWithPersonDynamic(
                                    apartment.getHepMBR(),
                                    apartment.getPerson() != null
                                            ? apartment.getPerson().getFirstName()
                                            : "",
                                    apartment.getBuilding().getCity().getName(),
                                    apartment.getBuilding().getAddresses().stream()
                                            .map(BuildingAddress::getAddressLine)
                                            .collect(Collectors.joining(",")),
                                    // ovdje spremamo sequence (ključno za sortiranje u Excelu)
                                    String.valueOf(apartment.getSequence()),
                                    monthly
                            )
                    );
                },

                // WRITER
                this::writeToXlsxKumulativnoMeterDynamicSafe
        );
    }

    //UNUSED
    public byte[] exportDataForBuildingsWithPersonKumulativnoByMetersDynamic(ExportDataViewModel dataViewModel) throws IOException {
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
                // Collect rows: each apartment's total meter value + person name
                List<YearMonth> months =
                        getMonthsFromOctober(dataViewModel.getYear(), dataViewModel.getMonth());

                List<MeasurementRowWithPersonDynamic> rows =
                        building.getApartments().stream()
                                .filter(a -> Boolean.TRUE.equals(a.getActive()))
                                .sorted(Comparator.comparingInt(a -> Optional.ofNullable(a.getSequence()).orElse(0)))
                                .flatMap(apartment ->
                                        apartment.getMeters().stream()
                                                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                                                .map(meter -> {

                                                    Map<YearMonth, Integer> cumulative =
                                                            sumMeasurementsByMonth(meter);

                                                    Map<YearMonth, MonthValue> monthlyValues =
                                                            calculateMonthlyValues(cumulative, months);

                                                    return new MeasurementRowWithPersonDynamic(
                                                            apartment.getHepMBR(),
                                                            apartment.getPerson() != null
                                                                    ? apartment.getPerson().getFirstName()
                                                                    : "",
                                                            apartment.getBuilding().getCity().getName(),
                                                            apartment.getBuilding().getAddresses().stream()
                                                                    .map(BuildingAddress::getAddressLine)
                                                                    .collect(Collectors.joining(",")),
                                                            meter.getCode(),
                                                            monthlyValues
                                                    );
                                                })

                                )
                                .toList();



                // Create CSV
                byte[] csvBytes = writeToXlsxKumulativnoMeterDynamic(rows,months);
                //writeToCsvWithPersonForMeter(rows);

                // Add CSV to ZIP
                String fileName = building.getName() + "_" + dataViewModel.getDate() + ".csv";
                ZipEntry entry = new ZipEntry(fileName);
                zipOut.putNextEntry(entry);
                zipOut.write(csvBytes);
                zipOut.closeEntry();

                // Optional: store locally
                helperService.storeCsvLocally(fileName, csvBytes);
            }
        }

        return zipBos.toByteArray();
    }

    private List<YearMonth> getMonthsFromOctober(int year, int selectedMonth) {
        List<YearMonth> months = new ArrayList<>();

        YearMonth start = YearMonth.of(year, 10);

        // If selected month is before October, it belongs to next year
        YearMonth end = selectedMonth >= 10
                ? YearMonth.of(year, selectedMonth)
                : YearMonth.of(year + 1, selectedMonth);

        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months;
    }

    private Map<YearMonth, Integer> sumMeasurementsByMonth(Meter meter) {
        return meter.getMeasurements().stream()
                .filter(m -> m.getValue() != null)
                .collect(Collectors.groupingBy(
                        m -> YearMonth.from(m.getMeasureDate()),
                        LinkedHashMap::new,
                        Collectors.summingInt(m -> m.getValue().intValue())
                ));
    }

    private byte[] writeToXlsxKumulativnoMeterDynamic(
            List<MeasurementRowWithPersonDynamic> rows,
            List<YearMonth> months
    ) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet("Export");

            createHeader(sheet.createRow(0), months);
            fillRows(sheet, rows, months);

            workbook.write(bos);
            return bos.toByteArray();
        }
    }


    private void fillRows(
            XSSFSheet sheet,
            List<MeasurementRowWithPersonDynamic> rows,
            List<YearMonth> months
    ) {
        int rowNum = 1;

        for (MeasurementRowWithPersonDynamic r : rows) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(r.getGrad());
            row.createCell(1).setCellValue(r.getAdresa());
            row.createCell(2).setCellValue(r.getHepMbr());
            row.createCell(3).setCellValue(r.getPersonName());
            row.createCell(4).setCellValue(r.getMeterCode());

            int col = 5;
            for (YearMonth ym : months) {
                MonthValue mv = r.getMonthlyValues().get(ym);

                row.createCell(col++)
                        .setCellValue(mv != null ? mv.getReading() : 0);

                row.createCell(col++)
                        .setCellValue(mv != null ? mv.getDiff() : 0);
            }
        }
    }




    private void createHeader(Row header, List<YearMonth> months) {

        header.createCell(0).setCellValue("Grad");
        header.createCell(1).setCellValue("Adresa");
        header.createCell(2).setCellValue("Hep Mbr");
        header.createCell(3).setCellValue("Naziv osobe");
        header.createCell(4).setCellValue("Serijski broj");

        int col = 5;
        for (YearMonth ym : months) {
            String monthName = ym.getMonth()
                    .getDisplayName(TextStyle.FULL, new Locale("hr"));

            header.createCell(col++).setCellValue(monthName + " stanje");
            header.createCell(col++).setCellValue(monthName + " potrošnja");
        }
    }


    private Map<YearMonth, MonthValue> calculateMonthlyValues(
            Map<YearMonth, Integer> cumulativeValues,
            List<YearMonth> months
    ) {
        Map<YearMonth, MonthValue> result = new LinkedHashMap<>();

        int previous = 0;

        for (YearMonth ym : months) {
            int current = cumulativeValues.getOrDefault(ym, 0);
            int diff = Math.max(current - previous, 0);

            result.put(ym, new MonthValue(current, diff));
            previous = current;
        }

        return result;
    }

    private byte[] writeToXlsxKumulativnoMeterDynamicSafe(
            List<MeasurementRowWithPersonDynamic> rows,
            List<YearMonth> months
    ) {
        try {
            return writeToXlsxKumulativnoMeterDynamic(rows, months);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
