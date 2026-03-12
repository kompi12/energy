package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "building_address",
        indexes = {
                @Index(name = "ix_building_address_building", columnList = "building_id"),
                @Index(name = "ix_building_address_city", columnList = "city"),
                @Index(name = "ix_building_address_postal_code", columnList = "postal_code")
        })
@Getter @Setter
@ToString(exclude = "building")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BuildingAddress {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "building_address_seq")
    @SequenceGenerator(name = "building_address_seq", sequenceName = "building_address_seq", allocationSize = 50)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_building_address_building"))
    private Building building;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country = "HR";
}
