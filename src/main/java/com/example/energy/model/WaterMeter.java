package com.example.energy.model;

import com.example.energy.enums.WaterMeterType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Table(name = "water_meter",
        uniqueConstraints = @UniqueConstraint(name = "uk_water_meter_code", columnNames = "code"),
        indexes = @Index(name = "ix_water_meter_apartment", columnList = "apartment_id"))
@Getter @Setter
@ToString(exclude = {"apartment", "measurements"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class WaterMeter {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "water_meter_seq")
    @SequenceGenerator(name = "water_meter_seq", sequenceName = "water_meter_seq", allocationSize = 1)
    @Column(name = "water_meter_id")
    private Long id;

    @Column(name = "code", unique = true, length = 50, nullable = false)
    private String code;

    @Column(name = "power", length = 50)
    private String power;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_water_meter_apartment"))
    private Apartment apartment;

    @Column(name = "active")
    private Boolean active = true;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "waterMeter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Measurement> measurements;

    @Enumerated(EnumType.STRING)
    @Column(name = "water_meter_type", nullable = false, length = 30)
    private WaterMeterType waterMeterType;

}
