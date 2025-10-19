package com.example.energy.model;

import com.example.energy.model.Apartment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 255)
    private String contact;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Apartment> apartments = new java.util.ArrayList<>();

    public void addApartment(Apartment a) {
        if (a == null) return;
        if (!apartments.contains(a)) {
            apartments.add(a);
            a.setPerson(this);
        }
    }

    public void removeApartment(Apartment a) {
        if (a == null) return;
        apartments.remove(a);
        a.setPerson(null);
    }
}
