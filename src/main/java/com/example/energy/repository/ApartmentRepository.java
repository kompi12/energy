package com.example.energy.repository;

import com.example.energy.model.Apartment;
import com.example.energy.model.Building;
import com.example.energy.model.Energy;
import com.example.energy.model.Person;
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


}