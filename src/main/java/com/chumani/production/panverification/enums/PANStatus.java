package com.chumani.production.panverification.enums;

/**
 * PAN Status Enumeration
 * Based on Protean API response values
 */
public enum PANStatus {
    ACTIVE("Active", "PAN is active and valid"),
    INACTIVE("Inactive", "PAN is inactive"),
    DEACTIVATED("Deactivated", "PAN has been deactivated"),
    CANCELLED("Cancelled", "PAN has been cancelled");

    private final String displayName;
    private final String description;

    PANStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PANStatus fromString(String status) {
        if (status == null) return null;

        for (PANStatus panStatus : PANStatus.values()) {
            if (panStatus.displayName.equalsIgnoreCase(status) ||
                panStatus.name().equalsIgnoreCase(status)) {
                return panStatus;
            }
        }
        throw new IllegalArgumentException("Unknown PAN status: " + status);
    }
}