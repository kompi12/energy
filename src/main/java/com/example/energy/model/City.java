package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "city",
        uniqueConstraints = @UniqueConstraint(name = "uk_city_name_country", columnNames = {"name", "country"}),
        indexes = @Index(name = "ix_city_name", columnList = "name"))
@Getter @Setter @ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class City {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_seq")
    @SequenceGenerator(name = "city_seq", sequenceName = "city_seq", allocationSize = 50)
    @Column(name = "city_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "country", nullable = false, length = 2)
    private String country = "HR";
}
