package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Meter;
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

    @Query("SELECT m FROM Meter m " +
            "LEFT JOIN FETCH m.apartment a " +
            "LEFT JOIN FETCH a.building b " +
            "LEFT JOIN FETCH b.city c " +
            "LEFT JOIN FETCH a.person p " +
            "WHERE p.firstName LIKE %:name%")
    List<Meter> findByPersonFirstNameLike(@Param("name") String name);


    @Query("SELECT m FROM Meter m " +
            "WHERE m.code = :pNumber and m.active is true")
    Optional<Meter> findByCodeAndActiveTrue(String pNumber);

    Optional<Meter> findByCodeAndApartment_Id(String code, Long apartmentId);
}