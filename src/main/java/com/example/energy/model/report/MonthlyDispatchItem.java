package com.example.energy.model.report;

import com.example.energy.model.Building;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_dispatch_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_dispatch_item_batch_building",
                columnNames = {"batch_id", "building_id"}
        ),
        indexes = {
                @Index(name = "ix_dispatch_item_batch", columnList = "batch_id"),
                @Index(name = "ix_dispatch_item_building", columnList = "building_id"),
                @Index(name = "ix_dispatch_item_status", columnList = "status")
        }
)
@Getter @Setter
@NoArgsConstructor
public class MonthlyDispatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dispatch_item_seq")
    @SequenceGenerator(name = "dispatch_item_seq", sequenceName = "dispatch_item_seq", allocationSize = 50)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dispatch_item_batch"))
    private MonthlyDispatchBatch batch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dispatch_item_building"))
    private Building building;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DispatchStatus status = DispatchStatus.NOT_SENT;

    @Column(name = "last_note", length = 1000)
    private String lastNote;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt asc")
    private List<MonthlyDispatchEvent> events = new ArrayList<>();
}
