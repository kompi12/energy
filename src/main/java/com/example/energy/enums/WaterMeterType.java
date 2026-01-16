package com.example.energy.enums;

public enum WaterMeterType {
    COLD_TECHEM,
    HOT_TECHEM,
    COLD_KAMSTRUP,
    HOT_KAMSTRUP;

    public boolean isCold() {
        return this == COLD_TECHEM || this == COLD_KAMSTRUP;
    }

    public boolean isHot() {
        return this == HOT_TECHEM || this == HOT_KAMSTRUP;
    }

    public boolean isTechem() {
        return this == COLD_TECHEM || this == HOT_TECHEM;
    }

    public boolean isKamstrup() {
        return this == COLD_KAMSTRUP || this == HOT_KAMSTRUP;
    }

    public boolean isColdTechem() {
        return this == COLD_TECHEM;
    }

    public boolean isHotTechem() {
        return this == HOT_TECHEM;
    }

    public boolean isColdKamstrup() {
        return this == COLD_KAMSTRUP;
    }

    public boolean isHotKamstrup() {
        return this == HOT_KAMSTRUP;
    }
}
