package com.example.energy.repository.report;

import com.example.energy.model.report.MonthlyDispatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MonthlyDispatchEventRepository extends JpaRepository<MonthlyDispatchEvent, Long> {

    @Query("""
      select e
      from MonthlyDispatchEvent e
      where e.item.id = :itemId
      order by e.createdAt asc
    """)
    List<MonthlyDispatchEvent> findTimeline(Long itemId);
}
