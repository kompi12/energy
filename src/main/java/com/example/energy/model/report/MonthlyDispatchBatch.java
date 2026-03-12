package com.example.energy.model.report;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_dispatch_batch",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dispatch_batch_type_month",
                columnNames = {"dispatch_type", "month_ym"}
        ),
        indexes = {
                @Index(name = "ix_dispatch_batch_month", columnList = "month_ym"),
                @Index(name = "ix_dispatch_batch_type", columnList = "dispatch_type")
        }
)
@Getter @Setter
@NoArgsConstructor
public class MonthlyDispatchBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dispatch_batch_seq_gen")
    @SequenceGenerator(name = "dispatch_batch_seq_gen", sequenceName = "dispatch_batch_seq", allocationSize = 1)
    @Column(name = "batch_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_type", nullable = false, length = 20)
    private DispatchType dispatchType;

    // spremamo kao "YYYY-MM" string radi jednostavne SQL filtracije
    @Column(name = "month_ym", nullable = false, length = 7)
    private String monthYm;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlyDispatchItem> items = new ArrayList<>();

    public MonthlyDispatchBatch(DispatchType type, YearMonth ym, String createdBy) {
        this.dispatchType = type;
        this.monthYm = ym.toString(); // "2026-02"
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }
}
