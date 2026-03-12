package com.example.energy.model.report;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "monthly_dispatch_event",
        indexes = {
                @Index(name = "ix_dispatch_event_item", columnList = "item_id"),
                @Index(name = "ix_dispatch_event_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor
public class MonthlyDispatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dispatch_event_seq")
    @SequenceGenerator(name = "dispatch_event_seq", sequenceName = "dispatch_event_seq", allocationSize = 50)
    @Column(name = "event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dispatch_event_item"))
    private MonthlyDispatchItem item;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private DispatchEventType eventType;

    @Column(name = "note", length = 2000)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private DispatchStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 20)
    private DispatchStatus toStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
