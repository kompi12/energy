package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "apartment",
        indexes = {
                @Index(name = "ix_apartment_building", columnList = "building_id"),
                @Index(name = "ix_apartment_person", columnList = "person_id")
        })
@Getter @Setter
@ToString(exclude = {"building", "person", "meters"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Apartment {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "apartment_seq")
    @SequenceGenerator(name = "apartment_seq", sequenceName = "apartment_seq", allocationSize = 50)
    @Column(name = "apartment_id")
    private Long id;

    @Column(name = "apartment_number", length = 20)
    private String apartmentNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_apartment_building"))
    private Building building;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_apartment_person"))
    private Person person;

    @Column(nullable = false, length = 100)
    private String mbr;

    @Column(name = "hep_mbr", length = 100)
    private String hepMBR;

    @Column
    private Integer priority;

    @Column(name = "sequence")
    private Integer sequence;

    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Meter> meters;
}
