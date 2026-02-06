package com.example.energy.controller;

import com.example.energy.model.Meter;
import com.example.energy.response.EnergyResponse;
import com.example.energy.service.MeterService;
import com.example.energy.viewmodel.MeterViewModel;
import com.example.energy.viewmodel.dto.MeterPersonViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonMultipleViewModel;
import com.example.energy.viewmodel.dto.RequestBodyPersonViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meter")
public class MeterController {

    @Autowired
    MeterService meterService;

    public EnergyResponse<List<Meter>> getAllMeter() {
        try {
            List<Meter> listOfPeople = meterService.findAll();
            return EnergyResponse.success("All people found", listOfPeople);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/meterForPerson")
    public EnergyResponse<List<MeterViewModel>> getMetersForPerson(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {
            List<MeterViewModel> listOfMeters = new ArrayList<>();
            List<Meter> listOfMetersForPerson = meterService.findAllForPersonName(viewModel.getName());
            for(Meter meter : listOfMetersForPerson) {
                listOfMeters.add(MeterViewModel.createViewModel(meter));
            }
            listOfMeters.get(0).setList(
                    Collections.singletonList(listOfMeters.stream()
                            .map(MeterViewModel::getCode)
                            .collect(Collectors.joining(",")))
            );


            return EnergyResponse.success("All meters found", listOfMeters);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/allMeterForPerson")
    public EnergyResponse<MeterPersonViewModel> getAllMetersForPerson(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {

            MeterPersonViewModel meterPersonViewModel = meterService.findAllForPersonMeter(viewModel.getName());
            return EnergyResponse.success("All meters found", meterPersonViewModel);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/deactivateMeter")
    public EnergyResponse<Boolean> deactivateMeter(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {
            Boolean  deactivate = meterService.deactivateMeter(viewModel.getCode());

            return EnergyResponse.success("All meters found", deactivate);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/deactivateMeters")
    public EnergyResponse<Boolean> deactivateMeters(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
        try {
            Boolean  deactivate = meterService.deactivateMeters(viewModel);

            return EnergyResponse.success("All meters found", deactivate);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/createMeter")
    public EnergyResponse<Boolean> createNewMeter(@RequestBody RequestBodyPersonViewModel viewModel) {
        try {
            Boolean createdMeter = meterService.createMeter(viewModel);

            return EnergyResponse.success("All meters found", createdMeter);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/createMeters")
    public EnergyResponse<Boolean> createNewMeters(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
        try {
            Boolean createdMeter = meterService.createMeters(viewModel);

            return EnergyResponse.success("All meters found", createdMeter);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }

    @RequestMapping("/createMetersDeactivateOld")
    public EnergyResponse<Boolean> createMetersDeactivateOld(@RequestBody RequestBodyPersonMultipleViewModel viewModel) {
        try {
            Boolean createdMeter = meterService.createMetersDeactivateOld(viewModel);

            return EnergyResponse.success("All meters found", createdMeter);

        } catch (Exception exception) {
            exception.printStackTrace();
            return EnergyResponse.error(500, exception.getMessage());
        }
    }


}
