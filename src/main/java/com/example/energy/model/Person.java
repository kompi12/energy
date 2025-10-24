package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "person",
        indexes = @Index(name = "ix_person_last_first", columnList = "last_name, first_name"))
@Getter @Setter
@ToString(exclude = "apartments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Person {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_seq")
    @SequenceGenerator(name = "person_seq", sequenceName = "person_seq", allocationSize = 50)
    @Column(name = "person_id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "contact", length = 255)
    private String contact;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Apartment> apartments = new ArrayList<>();

    public void addApartment(Apartment a) {
        if (a == null) return;
        if (!apartments.contains(a)) {
            apartments.add(a);
            a.setPerson(this);
        }
    }
}
