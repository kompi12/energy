package com.example.energy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "meter",
        uniqueConstraints = @UniqueConstraint(name = "uk_meter_code", columnNames = "code"),
        indexes = @Index(name = "ix_meter_apartment", columnList = "apartment_id"))
@Getter @Setter
@ToString(exclude = {"apartment", "measurements"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Meter {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "meter_seq")
    @SequenceGenerator(name = "meter_seq", sequenceName = "meter_seq", allocationSize = 1)
    @Column(name = "meter_id")
    private Long id;

    @Column(name = "code", unique = true, length = 50, nullable = false)
    private String code;

    @Column(name = "power", length = 50)
    private String power;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_meter_apartment"))
    private Apartment apartment;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Measurement> measurements;
}
