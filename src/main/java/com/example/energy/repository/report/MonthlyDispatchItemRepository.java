package com.example.energy.repository.report;

import com.example.energy.model.report.DispatchStatus;
import com.example.energy.model.report.MonthlyDispatchItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MonthlyDispatchItemRepository extends JpaRepository<MonthlyDispatchItem, Long> {

    @Query("""
      select i
      from MonthlyDispatchItem i
      join fetch i.building b
      left join fetch b.addresses
      left join fetch b.city
      where i.batch.id = :batchId
      order by coalesce(b.name, ''), b.code
    """)
    List<MonthlyDispatchItem> findAllByBatchIdWithBuilding(Long batchId);

    Optional<MonthlyDispatchItem> findByBatch_IdAndBuilding_Id(Long batchId, Long buildingId);

    @Query("""
      select i
      from MonthlyDispatchItem i
      join i.building b
      left join b.city c
      left join b.addresses a
      where i.batch.id = :batchId
        and (:status is null or i.status = :status)
        and (:city is null or lower(c.name) = lower(:city) or lower(a.city) = lower(:city))
        and (
          :q is null
          or lower(coalesce(b.name,'')) like lower(concat('%', :q, '%'))
          or lower(coalesce(b.code,'')) like lower(concat('%', :q, '%'))
          or lower(coalesce(a.addressLine,'')) like lower(concat('%', :q, '%'))
        )
      group by i, b, c
      order by coalesce(b.name,''), b.code
    """)
    Page<MonthlyDispatchItem> pageByBatch(
            @Param("batchId") Long batchId,
            @Param("status") DispatchStatus status,
            @Param("city") String city,
            @Param("q") String q,
            Pageable pageable
    );
}
