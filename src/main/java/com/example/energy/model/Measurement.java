package com.example.energy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "measurement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "month", nullable = true)
    private String month;

    @Column(name = "year", nullable = true)
    private String year;

    @Column(name = "day", nullable = true)
    private String day;

    @Column(nullable = true)
    private Integer value;

    @Column(nullable = true)
    private Date created;

    @Column(nullable = true)
    private Date updated;

    //later User createdBy
    @Column(nullable = true)
    private String createdBy;

}
