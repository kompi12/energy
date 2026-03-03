package com.example.energy.model.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_event", indexes = {
        @Index(name = "ix_audit_event_time", columnList = "created_at"),
        @Index(name = "ix_audit_event_user", columnList = "username"),
        @Index(name = "ix_audit_event_entity", columnList = "entity_type, entity_id")
})
@Getter @Setter
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_event_id")
    private Long id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    private String username;

    @Column(nullable = false, length = 50)
    private String action; // ENTITY_UPDATE / ENTITY_CREATE / ENTITY_DELETE

    @Column(name = "entity_type", length = 80)
    private String entityType;

    @Column(name = "entity_id", length = 80)
    private String entityId;

    @Column(columnDefinition = "jsonb")
    private String details; // {"old":{...},"new":{...}}
}
