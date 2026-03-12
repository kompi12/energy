package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "building",
        uniqueConstraints = @UniqueConstraint(name = "uk_building_code", columnNames = "code"),
        indexes = {
                @Index(name = "ix_building_city", columnList = "city_id"),
                @Index(name = "ix_building_code", columnList = "code")
        })
@Getter @Setter
@ToString(exclude = {"addresses", "apartments", "city"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Building {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "building_seq")
    @SequenceGenerator(name = "building_seq", sequenceName = "building_seq", allocationSize = 50)
    @Column(name = "building_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String code;      // logical building code (unique)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_building_city"))
    private City city;

    @Column(length = 100)
    private String name;

    @Column(name = "water")
    private Boolean water = false;

    @Column(name = "heat")
    private Boolean heat = false;

    @Column(length = 100)
    private String techem;

    // One building can have multiple physical addresses
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BuildingAddress> addresses = new ArrayList<>();

    // If apartments point to Building (not address), keep this:
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Apartment> apartments = new ArrayList<>();
}
