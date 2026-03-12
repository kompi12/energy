package com.example.energy.model.report;

public enum DispatchStatus {
    NOT_SENT,    // nije poslano
    SENT,        // poslano
    ERROR,       // greška / problem
    FIXED_SENT   // bilo problem -> ispravljeno -> poslano
}
