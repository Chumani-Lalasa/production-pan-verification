package com.chumani.production.panverification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * PAN Verification Request DTO
 * Validates input according to business requirements
 */
public class PANVerificationRequest {

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{3}P[A-Z][0-9]{4}[A-Z]$",
             message = "PAN format must be: 3 alphabets + P + 1 alphabet + 4 digits + 1 alphabet")
    @Size(min = 10, max = 10, message = "PAN must be exactly 10 characters")
    private String pan;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    // Constructors
    public PANVerificationRequest() {}

    public PANVerificationRequest(String pan, String name) {
        this.pan = pan;
        this.name = name;
    }

    // Getters and Setters
    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Business methods for PII masking
    public String getMaskedPan() {
        if (pan == null || pan.length() != 10) return "INVALID_PAN";
        return "XXXX" + pan.substring(4, 8) + pan.substring(9);
    }

    public String getMaskedName() {
        if (name == null || name.length() <= 2) return "****";
        return name.substring(0, 2) + "****";
    }

    @Override
    public String toString() {
        return "PANVerificationRequest{" +
                "pan='" + getMaskedPan() + "'" +
                ", name='" + getMaskedName() + "'" +
                "}";
    }
}