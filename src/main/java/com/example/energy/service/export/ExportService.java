package com.example.energy.service.export;


import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;


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


    public void importData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // first sheet

            // Skip header row (start from row 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell objectNo = row.getCell(0);
                Cell StreetNo = row.getCell(1);
                Cell userName = row.getCell(2);
                Cell flatNo = row.getCell(3);
                Cell floor = row.getCell(4);
                Cell deviceNo = row.getCell(5);
                Cell type = row.getCell(6);
                Cell reading = row.getCell(7);
                Cell value = row.getCell(8);

                // Meter meter = meterRepository.findByCode(getCellValue(deviceNo));
//                Building building = buildingRepository.findBuildingById(getCellValue(objectNo));
//                Apartment apartment = apartmentRepository.findApartmentByBuilding(building);
//                if (!meter.getApartment().equals(apartment)) {
//                    //write in a log that the meter is wrong
//                }
//                ;
//
//                Measurement measurement = new Measurement();
//                measurement.setMeter(meter);
//                measurement.setMonth(new Date(getCellValue(reading).toString()));
//                measurement.setValue((Integer) getCellValue(value));
//                measurementRepository.save(measurement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void importDataForMonth(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row (start from row 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell sifraZgrade = row.getCell(0);
                Cell adresa = row.getCell(1);
                Cell nazivKorisnika = row.getCell(2);
                Cell serijskibrojExcel = row.getCell(5);
                Cell dateExcel = row.getCell(7); // 2024-12-26
                Cell valueExcel = row.getCell(8);
                DataFormatter formatter = new DataFormatter();
                if(serijskibrojExcel == null ){
                    continue;
                }
                String serijskki_broj =  formatter.formatCellValue(serijskibrojExcel);
                Meter meter = meterRepository.findByCode(serijskki_broj);
                Measurement measurement = new Measurement();
                if(meter != null) {
                    measurement.setMeter(meter);

                    if (valueExcel != null) {
                        String value = formatter.formatCellValue(valueExcel);
                        measurement.setValue(Integer.valueOf(value));

                    } else {
                        measurement.setValue(0);
                    }
                    if (dateExcel != null) {
                        String date = formatter.formatCellValue(dateExcel);
                        measurement.setYear(date.split("-")[0]);
                        measurement.setMonth(date.split("-")[1]);
                        measurement.setDay(date.split("-")[2]);
                    }
                    measurement.setCreated(new Date());
                    measurement.setCreatedBy("Import Excela");
                    measurementRepository.save(measurement);

                } else {
                    logger.info("{} {}", serijskki_broj, nazivKorisnika);
                }

            }
        }
    }


    public void importInitalData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // first sheet
            Sheet sheet2 = workbook.getSheetAt(1); // second sheet
            Sheet sheet3 = workbook.getSheetAt(2); // third sheet
            Sheet sheet4 = workbook.getSheetAt(3); // fourth sheet

            // Skip header row (start from row 1)
//            for (int i = 1; i <= sheet3.getLastRowNum(); i++) {
//                Row row = sheet3.getRow(i);
//                if (row == null) continue;
//
//                Cell sifraZgrade = row.getCell(1);
//                Cell grad = row.getCell(2);
//                Cell adresa = row.getCell(3);
//                Cell mbr = row.getCell(4);
//                Cell nazivKorisnika = row.getCell(5);
//                Cell serijskibroj = row.getCell(6);
//                Cell snaga = row.getCell(7);
//                Cell value = row.getCell(8);
//
//
//                if (findMeter(mbr.getStringCellValue(), adresa.getStringCellValue(), grad.getStringCellValue(), serijskibroj.getStringCellValue(), snaga.getStringCellValue(), sifraZgrade.getStringCellValue(), nazivKorisnika.getStringCellValue()) == null) {
//                    //logg the data not saved
//                    logger.info("User exists with apartment");
//                } else {
//                    //logg the data that is saved
//                    logger.info("User doesnt exists in the system");
//                }
//            }

            for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                Row row = sheet2.getRow(i);
                if (row == null) continue;


                Cell nazivKorisnika = row.getCell(5);
                Cell mbr = row.getCell(6);
                Cell hep_mbr = row.getCell(7);

                Apartment apartment = apartmentRepository.findApartmentByMbr(mbr.getStringCellValue());
                if ( apartment == null ) {
                    logger.info("User doesnt exists in the system + {}", nazivKorisnika.getStringCellValue());
                }
            }

            for (int i = 1; i <= sheet4.getLastRowNum(); i++) {
                Row row = sheet4.getRow(i);
                if (row == null) continue;


                Cell nazivKorisnika = row.getCell(5);
                Cell mbr = row.getCell(4);
                Cell hep_mbr = row.getCell(7);

                Apartment apartment = apartmentRepository.findApartmentByMbr(mbr.getStringCellValue());
                if ( apartment == null ) {
                    logger.info("User doesnt exists in the system + {}", nazivKorisnika.getStringCellValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Meter findMeter(String mbr, String adress, String city, String code, String power, String buildingCode, String personName) {

        Meter meterOld = meterRepository.findByCode(code);
        if (meterOld == null) {
            Meter meter = new Meter();
            meter.setApartment(findApartment(mbr, adress, city, buildingCode, personName));
            meter.setCode(code);
            meter.setPower(power);
            Meter meterReturn = meterRepository.save(meter);
            return meterReturn;
        } else {
            return meterOld;
        }

    }

    public Apartment findApartment(String mbr, String adress, String city, String buildingCode, String personName) {
        Apartment apartmentOld = apartmentRepository.findApartmentByMbr(mbr);
        Person personOld = personRepository.findPersonByFirstName(personName); // consider making this unique or using findByFirstNameIgnoreCase

        if (apartmentOld == null) {
            Apartment apartment = new Apartment();
            apartment.setMbr(mbr);
            apartment.setBuilding(findBuilding(city, adress, buildingCode));
            Apartment saved = apartmentRepository.save(apartment);

            if (personOld == null) {
                Person person = new Person();
                person.setFirstName(personName);
                person.addApartment(saved);
                personRepository.save(person);
            } else {
                personOld.addApartment(saved);
                personRepository.save(personOld);
            }
            return saved;

        } else {
            if (personOld == null) {
                Person person = new Person();
                person.setFirstName(personName);
                person.addApartment(apartmentOld);
                personRepository.save(person);
            } else {
                personOld.addApartment(apartmentOld);
                personRepository.save(personOld);
            }
            return apartmentOld;
        }
    }


    public Building findBuilding(String cityString, String adress, String buildingCode) {
        Building buildingOld = buildingRepository.findBuildingByAddress(adress);
        if (buildingOld == null) {
            Building building = new Building();
            building.setAddress(adress);
            building.setCode(buildingCode);
            building.setCity(findCity(cityString));
            return buildingRepository.save(building);
        } else {
            return buildingOld;
        }
    }

    public City findCity(String cityString) {
        City city = cityRepository.findByName(cityString);
        if (city == null) {
            City cityNew = new City();
            cityNew.setName(cityString);
            return cityRepository.save(cityNew);
        } else {
            return city;
        }
    }


    private Object getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue(); // returns java.util.Date
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Check if the number is an integer (whole number)
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue; // return Long
                    } else {
                        return numericValue; // return Double
                    }
                }

            case BOOLEAN:
                return cell.getBooleanCellValue(); // returns Boolean

            case FORMULA:
                // Evaluate formula if needed
                FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                return getCellValue(evaluator.evaluateInCell(cell)); // recursively get the evaluated value

            case BLANK:
                return null;

            default:
                return null;
        }
    }

}
