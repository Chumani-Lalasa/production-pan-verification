package com.chumani.production.panverification.entity;

import java.time.LocalDateTime;

import com.chumani.production.panverification.enums.PANStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * PAN Verification Record Entity
 * Stores complete audit trail of PAN verification requests and responses
 */
@Entity
@Table(name = "pan_verification_records", indexes = {
    @Index(name = "idx_pan_number", columnList = "panNumber"),
    @Index(name = "idx_reference_number", columnList = "referenceNumber"),
    @Index(name = "idx_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class PANVerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{3}P[A-Z][0-9]{4}[A-Z]$",
             message = "PAN format must be: 3 alphabets + P + 1 alphabet + 4 digits + 1 alphabet")
    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PANStatus status;

    @Column(name = "aadhaar_linked")
    private Boolean aadhaarLinked;

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 50)
    private String transactionId;

    @Column(name = "trace_id", length = 50)
    private String traceId;

    // Audit timestamps
    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimestamp;

    @Column(name = "response_timestamp")
    private LocalDateTime responseTimestamp;

    @Column(name = "persisted_timestamp", nullable = false)
    private LocalDateTime persistedTimestamp = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Error tracking
    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    // Constructors
    public PANVerificationRecord() {}

    public PANVerificationRecord(String panNumber, String name, PANStatus status,
                               Boolean aadhaarLinked, String referenceNumber, String transactionId) {
        this.panNumber = panNumber;
        this.name = name;
        this.status = status;
        this.aadhaarLinked = aadhaarLinked;
        this.referenceNumber = referenceNumber;
        this.transactionId = transactionId;
        this.requestTimestamp = LocalDateTime.now();
    }

    // Getters and Setters (explicit as per requirements - no Lombok)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PANStatus getStatus() { return status; }
    public void setStatus(PANStatus status) { this.status = status; }

    public Boolean getAadhaarLinked() { return aadhaarLinked; }
    public void setAadhaarLinked(Boolean aadhaarLinked) { this.aadhaarLinked = aadhaarLinked; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public LocalDateTime getRequestTimestamp() { return requestTimestamp; }
    public void setRequestTimestamp(LocalDateTime requestTimestamp) { this.requestTimestamp = requestTimestamp; }

    public LocalDateTime getResponseTimestamp() { return responseTimestamp; }
    public void setResponseTimestamp(LocalDateTime responseTimestamp) { this.responseTimestamp = responseTimestamp; }

    public LocalDateTime getPersistedTimestamp() { return persistedTimestamp; }
    public void setPersistedTimestamp(LocalDateTime persistedTimestamp) { this.persistedTimestamp = persistedTimestamp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    // Business methods for PII masking
    public String getMaskedPAN() {
        if (panNumber == null || panNumber.length() != 10) return "INVALID_PAN";
        return "XXXX" + panNumber.substring(4, 8) + panNumber.substring(9);
    }

    public String getMaskedName() {
        if (name == null || name.length() <= 2) return "****";
        return name.substring(0, 2) + "****";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "PANVerificationRecord{" +
                "id=" + id +
                ", panNumber='" + getMaskedPAN() + "'" +
                ", name='" + getMaskedName() + "'" +
                ", status=" + status +
                ", referenceNumber='" + referenceNumber + "'" +
                ", transactionId='" + transactionId + "'" +
                ", traceId='" + traceId + "'" +
                "}";
    }
}
