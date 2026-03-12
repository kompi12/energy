package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "measurement",
        indexes = {
                @Index(name = "ix_measurement_meter", columnList = "meter_id"),
                @Index(name = "ix_measurement_date", columnList = "measure_date")
        })
@Getter @Setter
@ToString(exclude = "meter")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Measurement {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "measurement_seq")
    @SequenceGenerator(name = "measurement_seq", sequenceName = "measurement_seq", allocationSize = 50)
    @Column(name = "measurement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id",
            foreignKey = @ForeignKey(name = "fk_measurement_meter"))
    private Meter meter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "water_meter",
            foreignKey = @ForeignKey(name = "fk_measurement_water_meter"))
    private WaterMeter waterMeter;

    @Column(name = "measure_date", nullable = false)
    private LocalDate measureDate;

    @Column(name = "value")
    private Double value;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
