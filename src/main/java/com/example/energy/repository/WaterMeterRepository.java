package com.example.energy.repository;

import com.example.energy.model.WaterMeter;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.DeviceSearchResultDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WaterMeterRepository extends JpaRepository<WaterMeter, Long> {
    Optional<WaterMeter> findByCode(String code);

    @Query("SELECT m FROM WaterMeter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id = :buildingId AND m.code = :id")
    Optional<WaterMeter> findByBuildingAndWaterMeter(@Param("buildingId") Long buildingId, @Param("id") Long id);


    @Query("SELECT m FROM WaterMeter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id IN (:buildingId) AND m.code = :id")
    Optional<WaterMeter> findByBuildingsAndWaterMeter(@Param("buildingId") List<Long> buildingId, @Param("id") Long id);

    Collection<Object> findByCodeIn(Set<String> waterWaterMeterCodes);

    List<WaterMeter> findByCodeInAndActiveTrue(Set<String> waterWaterMeterCodes);

    @Query("""
    SELECT m FROM WaterMeter m
    LEFT JOIN FETCH m.apartment a
    LEFT JOIN FETCH a.building b
    LEFT JOIN FETCH b.city c
    LEFT JOIN FETCH a.person p
    WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<WaterMeter> findByPersonFirstNameLikeIgnoreCase(@Param("name") String name);



    @Query("SELECT m FROM WaterMeter m " +
            "WHERE m.code = :pNumber and m.active is true")
    Optional<WaterMeter> findByCodeAndActiveTrue(String pNumber);

    Optional<WaterMeter> findByCodeAndApartment_Id(String code, Long apartmentId);

    List<WaterMeter> findByApartment_Id(Long apartmentId);
    long countByApartment_Building_Id(Long buildingId);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
        select wm.apartment.id, count(wm.id)
        from WaterMeter wm
        where wm.apartment.building.id = :buildingId
        group by wm.apartment.id
    """)
    List<Object[]> countByApartmentForBuilding(@Param("buildingId") Long buildingId);


    @Query("""
   select m.apartment.id, count(m)
   from WaterMeter m
   where m.apartment.building.id = :buildingId
     and m.active = true
   group by m.apartment.id
""")
    List<Object[]> countActiveByApartmentForBuilding(Long buildingId);

    @Query("""
    select new com.example.energy.viewmodel.dto.DeviceSearchResultDto(
        'WATER',
        wm.id,
        wm.code,
        wm.active,
        a.id,
        a.apartmentNumber,
        b.code
    )
    from WaterMeter wm
    join wm.apartment a
    join a.building b
    where lower(wm.code) like lower(concat('%', :q, '%'))
    order by b.code asc, a.apartmentNumber asc, wm.code asc
""")
    List<DeviceSearchResultDto> searchWaterMetersByCode(@Param("q") String q);

}