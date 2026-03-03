package com.example.energy.repository;

import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BuildingRepository extends JpaRepository<Building, Long> {
    Optional<Building> findByCodeIgnoreCaseAndCity_NameIgnoreCase(String code, String cityName);
    Optional<Building> findByCodeIgnoreCase(String code);

    @Query("""
       select distinct b
       from Building b
       left join fetch b.addresses
       left join fetch b.city
       """)
    List<Building> findAllWithAddresses();

    @Query("""
   select distinct b
   from Building b
   left join fetch b.city
   left join fetch b.addresses
""")
    List<Building> findAllForDispatch();
}