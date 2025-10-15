package com.example.energy.service.importer;


import com.example.energy.model.*;
import com.example.energy.repository.*;
import com.example.energy.service.ApartmentService;
import com.example.energy.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.expression.common.ExpressionUtils.toLong;

@Service
public class ImporterService {


    public ImporterService(
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

                Meter meter = meterRepository.findByCode(getCellValue(deviceNo));
                Building building = buildingRepository.findBuildingById(getCellValue(objectNo));
                Apartment apartment = apartmentRepository.findApartmentByBuilding(building);
                if (!meter.getApartment().equals(apartment)) {
                    //write in a log that the meter is wrong
                }
                ;

                Measurement measurement = new Measurement();
                measurement.setMeter(meter);
                measurement.setMonth(new Date(getCellValue(reading).toString()));
                measurement.setValue((Integer) getCellValue(value));
                measurementRepository.save(measurement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void importInitalData(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // first sheet
            Sheet sheet2 = workbook.getSheetAt(0); // second sheet
            Sheet sheet3 = workbook.getSheetAt(0); // third sheet
            Sheet sheet4 = workbook.getSheetAt(0); // fourth sheet

            // Skip header row (start from row 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell sifraZgrade = row.getCell(1);
                Cell grad = row.getCell(2);
                Cell adresa = row.getCell(3);
                Cell mbr = row.getCell(4);
                Cell nazivKorisnika = row.getCell(5);
                Cell serijskibroj = row.getCell(6);
                Cell snaga = row.getCell(7);
                Cell value = row.getCell(8);


                if(findMeter(mbr.getStringCellValue(), adresa.getStringCellValue(), grad.getStringCellValue(), serijskibroj.getStringCellValue(),snaga.getStringCellValue(), sifraZgrade.getStringCellValue(), nazivKorisnika.getStringCellValue()) == null) {
                    //logg the data not saved
                }else {
                    //logg the data that is saved
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Meter findMeter(String mbr, String adress, String city, String code,String power,String buildingCode,String personName) {

        Meter meterOld = meterRepository.findByCode(code);
        if (meterOld == null) {
            Meter meter = new Meter();
            meter.setApartment(findApartment(mbr, adress, city,buildingCode,personName));
            meter.setPower(power);
            return meter;
        } else {
            return meterOld;
        }

    }

    public Apartment findApartment(String mbr, String adress, String city,String buildingCode,String personName) {
        Apartment apartmentOld = apartmentRepository.findApartmentByMbr(mbr);
        Person personOld = personRepository.findPersonByFirstName(personName);
        if (apartmentOld == null) {
            Apartment apartment = new Apartment();
            apartment.setMbr(mbr);
            apartment.setBuilding(findBuilding(city, adress,buildingCode));

          Apartment app =  apartmentRepository.save(apartment);
          List<Apartment> apartmentList = new ArrayList<>();
          apartmentList.add(app);
          if(personOld == null) {
              Person person = new Person();
              person.setFirstName(personName);
              person.setApartments(apartmentList);
              personRepository.save(person);
          } else {
              personOld.setApartments(apartmentList);
              personRepository.save(personOld);
          }
          return app;
        } else {
            List<Apartment> apartmentList = new ArrayList<>();
            apartmentList.add(apartmentOld);
            if(personOld == null) {
                Person person = new Person();
                person.setFirstName(personName);
                person.setApartments(apartmentList);
                personRepository.save(person);
            } else {
                personOld.setApartments(apartmentList);
                personRepository.save(personOld);
            }
            return apartmentOld;
        }
    }

    public Building findBuilding(String cityString, String adress,String buildingCode) {
        Building buildingOld = buildingRepository.findBuildingByName(cityString);
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
