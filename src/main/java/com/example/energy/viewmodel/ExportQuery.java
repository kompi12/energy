package com.example.energy.viewmodel;

public record ExportQuery(
        String mode,      // DETAIL | SUMMARY
        String from,      // YYYY-MM
        String to         // YYYY-MM
) {}
