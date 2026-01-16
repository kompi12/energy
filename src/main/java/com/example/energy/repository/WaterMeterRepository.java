package com.example.energy.repository;

import com.example.energy.model.WaterMeter;
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
}