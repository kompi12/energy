package com.example.energy.controller;

import com.example.energy.model.WaterMeter;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.ApartmentService;
import com.example.energy.service.MeasurementService;
import com.example.energy.service.WaterMeterService;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.WaterMeterPersonViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonMultipleViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonViewModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/waterMeters")
@CrossOrigin(origins = {"http://localhost:5173"})
public class WaterMeterController {

    private final ApartmentService apartmentService;
    private final MeasurementService measurementService;
    private final WaterMeterService waterMeterService;

    public WaterMeterController(ApartmentService apartmentService, MeasurementService measurementService, WaterMeterService waterMeterService) {
        this.apartmentService = apartmentService;
        this.measurementService = measurementService;
        this.waterMeterService = waterMeterService;
    }

    public EnergyResponse<List<WaterMeter>> getAllWaterMeter() {
        try {
            List<WaterMeter> listOfPeople = waterMeterService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }
//
//    @RequestMapping("/waterMeterForPerson")
//    public EnergyResponse<List<WaterMeterViewModel>> getWaterMetersForPerson(@RequestBody RequestBodyPersonViewModel viewModel) {
//        try {
//            List<WaterMeterViewModel> listOfWaterMeters = new ArrayList<>();
//            List<WaterMeter> listOfWaterMetersForPerson = waterMeterService.findAllForPersonName(viewModel.getName());
//            for(WaterMeter waterMeter : listOfWaterMetersForPerson) {
//                listOfWaterMeters.add(WaterMeterViewModel.createViewModel(waterMeter));
//            }
//            listOfWaterMeters.get(0).setList(
//                    Collections.singletonList(listOfWaterMeters.stream()
//                            .map(WaterMeterViewModel::getCode)
//                            .collect(Collectors.joining(",")))
//            );
//
//
//            return EnergyResponse.success("All waterMeters found", listOfWaterMeters);
//
//        } catch (Exception exception) {
//            exception.printStackTrace();
//            return EnergyResponse.error(500, exception.getMessage());
//        }
//    }

    @RequestMapping("/allWaterMeterForPerson")
    public EnergyResponse<WaterMeterPersonViewModel> getAllWaterMetersForPerson(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {

            WaterMeterPersonViewModel waterMeterPersonViewModel = waterMeterService.findAllForPersonWaterMeter(viewModel.getName());
            return EnergyResponse.success("All waterMeters found", waterMeterPersonViewModel);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/deactivateWaterMeter")
    public EnergyResponse<Boolean> deactivateWaterMeter(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {
            Boolean  deactivate = waterMeterService.deactivateWaterMeter(viewModel.getCode());

            return EnergyResponse.success("All waterMeters found", deactivate);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/deactivateWaterMeters")
    public EnergyResponse<Boolean> deactivateWaterMeters(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
        try {
            Boolean  deactivate = waterMeterService.deactivateWaterMeters(viewModel);

            return EnergyResponse.success("All waterMeters found", deactivate);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/createWaterMeter")
    public EnergyResponse<Boolean> createNewWaterMeter(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {
            Boolean createdWaterMeter = waterMeterService.createWaterMeter(viewModel);

            return EnergyResponse.success("All waterMeters found", createdWaterMeter);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/createWaterMeters")
    public EnergyResponse<Boolean> createNewWaterMeters(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
        try {
            Boolean createdWaterMeter = waterMeterService.createWaterMeters(viewModel);

            return EnergyResponse.success("All waterMeters found", createdWaterMeter);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

//    @RequestMapping("/createWaterMetersDeactivateOld")
//    public EnergyResponse<Boolean> createWaterMetersDeactivateOld(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
//        try {
//            Boolean createdWaterMeter = waterMeterService.createWaterMetersDeactivateOld(viewModel);
//
//            return EnergyResponse.success("All waterMeters found", createdWaterMeter);
//
//        } catch (Exception exception) {
//            exception.printStackTrace();
//            return EnergyResponse.error(500, exception.getMessage());
//        }
//    }


    @PutMapping("/{id}")
    public DTO.WaterMeterDto update(@PathVariable Long id, @RequestBody DTO.DeviceUpdateRequest req) {
        return apartmentService.updateWaterMeter(id, req);
    }

    @GetMapping("/{id}/measurements")
    public List<DTO.MeasurementDto> measurements(@PathVariable Long id) {
        return measurementService.listForWaterMeter(id);
    }

    @PostMapping("/{id}/measurements")
    public DTO.MeasurementDto addMeasurement(@PathVariable Long id, @RequestBody DTO.MeasurementCreateRequest req) {
        return measurementService.addForWaterMeter(id, req);
    }
    


}
