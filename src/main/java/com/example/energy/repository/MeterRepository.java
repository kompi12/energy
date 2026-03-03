package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Meter;
import com.example.energy.viewmodel.dto.DTO;
import com.example.energy.viewmodel.dto.DeviceSearchResultDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Optional<Meter> findByCode(String code);

    @Query("SELECT m FROM Meter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id = :buildingId AND m.code = :id")
    Optional<Meter> findByBuildingAndMeter(@Param("buildingId") Long buildingId, @Param("id") Long id);


    @Query("SELECT m FROM Meter m " +
            "LEFT JOIN m.apartment a " +
            "WHERE a.building.id IN (:buildingId) AND m.code = :id")
    Optional<Meter> findByBuildingsAndMeter(@Param("buildingId") List<Long> buildingId, @Param("id") Long id);

    Collection<Object> findByCodeIn(Set<String> meterCodes);

    List<Meter> findByCodeInAndActiveTrue(Set<String> meterCodes);

    @Query("""
    SELECT m FROM Meter m
    LEFT JOIN FETCH m.apartment a
    LEFT JOIN FETCH a.building b
    LEFT JOIN FETCH b.city c
    LEFT JOIN FETCH a.person p
    WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
""")
    List<Meter> findByPersonFirstNameLikeIgnoreCase(@Param("name") String name);



    @Query("SELECT m FROM Meter m " +
            "WHERE m.code = :pNumber and m.active is true")
    Optional<Meter> findByCodeAndActiveTrue(String pNumber);

    Optional<Meter> findByCodeAndApartment_Id(String code, Long apartmentId);

    List<Meter> findAllByCode(String code);

    List<Meter> findAllByCodeAndActive(String code, Boolean active);

    List<Meter> findByApartment_Id(Long apartmentId);
    long countByApartment_Building_Id(Long buildingId);

    boolean existsByCodeIgnoreCase(String code);

    @Query("""
        select m.apartment.id, count(m.id)
        from Meter m
        where m.apartment.building.id = :buildingId
        group by m.apartment.id
    """)
    List<Object[]> countByApartmentForBuilding(@Param("buildingId") Long buildingId);


    @Query("""
   select m.apartment.id, count(m)
   from Meter m
   where m.apartment.building.id = :buildingId
     and m.active = true
   group by m.apartment.id
""")
    List<Object[]> countActiveByApartmentForBuilding(Long buildingId);
    List<Meter> findByApartment_Building_Id(Long buildingId);

    @Query("""
    select new com.example.energy.viewmodel.dto.DeviceSearchResultDto(
        'METER',
        m.id,
        m.code,
        m.active,
        a.id,
        a.apartmentNumber,
        b.code
    )
    from Meter m
    join m.apartment a
    join a.building b
    where lower(m.code) like lower(concat('%', :q, '%'))
    order by b.code asc, a.apartmentNumber asc, m.code asc
""")
    List<DeviceSearchResultDto> searchMetersByCode(@Param("q") String q);



}