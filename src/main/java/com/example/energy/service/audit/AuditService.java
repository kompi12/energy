package com.example.energy.service.audit;

import com.example.energy.model.audit.AuditEvent;
import com.example.energy.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuditService {

    private final AuditEventRepository repo;
    private final ObjectMapper om;

    public AuditService(AuditEventRepository repo, ObjectMapper om) {
        this.repo = repo;
        this.om = om;
    }

    private String currentUserOrNull() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return auth.getName();
    }

    public void logChangeIfDifferent(String entityType, String entityId, Object oldVal, Object newVal) {
        if (oldVal != null && oldVal.equals(newVal)) return;

        logEntityChange("ENTITY_UPDATE", entityType, entityId, oldVal, newVal);
    }

    public void logEntityChange(String action, String entityType, String entityId, Object oldVal, Object newVal) {
        try {
            AuditEvent e = new AuditEvent();
            e.setAction(action);
            e.setUsername(currentUserOrNull());
            e.setEntityType(entityType);
            e.setEntityId(entityId);

            Map<String, Object> details = new java.util.LinkedHashMap<>();
            details.put("old", oldVal);
            details.put("new", newVal);
            e.setDetails(om.writeValueAsString(details));
            repo.save(e);
        } catch (Exception ignored) {
            // audit nikad ne smije rušiti business logiku
        }
    }
}
