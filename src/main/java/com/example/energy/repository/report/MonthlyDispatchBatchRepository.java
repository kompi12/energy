package com.example.energy.repository.report;

import com.example.energy.model.report.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyDispatchBatchRepository extends JpaRepository<MonthlyDispatchBatch, Long> {
    Optional<MonthlyDispatchBatch> findByDispatchTypeAndMonthYm(DispatchType type, String monthYm);

    List<MonthlyDispatchBatch> findAllByDispatchTypeAndMonthYmOrderByIdDesc(DispatchType dispatchType, String monthYm);
    List<MonthlyDispatchBatch> findAllByDispatchTypeOrderByIdDesc(DispatchType dispatchType);
    List<MonthlyDispatchBatch> findAllByMonthYmOrderByIdDesc(String monthYm);
}
