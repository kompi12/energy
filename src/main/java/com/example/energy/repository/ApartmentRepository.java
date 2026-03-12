package com.example.energy.repository;

import com.example.energy.model.Apartment;
import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import com.example.energy.model.Person;
import com.example.energy.viewmodel.dto.ApartmentSearchResultDto;
import com.example.energy.viewmodel.dto.DTO;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
    Optional<Apartment> findByMbr(String mbr);

    Optional<Apartment> findByHepMBR(String hepMBR);

    Optional<Apartment> findByMjernoMjesto(String mjernoMjesto);

    Apartment findApartmentById(Long id);

    Apartment findByPerson(Person person);


    @Query("""
        select distinct a
        from Apartment a
        left join fetch a.meters m
        left join fetch a.person
        where a.building = :building
          and a.active = true
    """)
    List<Apartment> findApartmentsWithMeters(@Param("building") Building building);


    @Query("""
    select a
    from Apartment a
    join a.person p
    where lower(p.firstName) like lower(concat('%', :firstName, '%'))
""")
    List<Apartment> findApartmentsByPersonFirstNameLike(
            @Param("firstName") String firstName
    );


    @Query("""
        select a
        from Apartment a
        left join fetch a.person p
        where a.building.id = :buildingId
        order by a.apartmentNumber
    """)
    List<Apartment> findByBuildingWithPerson(@Param("buildingId") Long buildingId);

    @Query("""
        select a
        from Apartment a
        left join fetch a.person
        where a.id = :id
    """)
    Optional<Apartment> findByIdWithPerson(@Param("id") Long id);

    long countByBuilding_Id(Long buildingId);

    @Query("""
        select count(distinct p.id)
        from Apartment a
        join a.person p
        where a.building.id = :buildingId
    """)
    long countDistinctPersonsInBuilding(@Param("buildingId") Long buildingId);

    List<Apartment> findByBuilding_Id(Long buildingId);


        @Query("""
        select a
        from Apartment a
        left join fetch a.person p
        where a.building.id = :buildingId
    """)
        List<Apartment> findForBuildingWithPerson(@Param("buildingId") Long buildingId);

    @Query("""
    select new com.example.energy.viewmodel.dto.ApartmentSearchResultDto(
        a.id,
        a.apartmentNumber,
        b.code,
        b.name,
        c.name,
        a.hepMBR,
        a.hepMBRWater,
        a.mbr,
        a.mjernoMjesto,
        trim(concat(coalesce(p.firstName,''),' ',coalesce(p.lastName,''))),
        p.contact
    )
    from Apartment a
    join a.building b
    left join b.city c
    left join a.person p
    where
      lower(coalesce(a.apartmentNumber,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(a.mbr,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(a.hepMBR,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(a.hepMBRWater,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(a.mjernoMjesto,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(b.code,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(b.name,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(c.name,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(p.firstName,'')) like lower(concat('%', :q, '%'))
      or lower(coalesce(p.lastName,'')) like lower(concat('%', :q, '%'))
      or lower(concat(coalesce(p.firstName,''),' ',coalesce(p.lastName,''))) like lower(concat('%', :q, '%'))
      or lower(coalesce(p.contact,'')) like lower(concat('%', :q, '%'))
    order by b.code asc, a.apartmentNumber asc
""")
    List<ApartmentSearchResultDto> searchApartments(@Param("q") String q);

}