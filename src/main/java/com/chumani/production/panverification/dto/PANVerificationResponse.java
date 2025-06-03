package com.chumani.production.panverification.dto;

import com.chumani.production.panverification.enums.PANStatus;
import java.time.LocalDateTime;

/**
 * PAN Verification Response DTO
 * Contains verification result and audit information
 */
public class PANVerificationResponse {

    private String referenceNumber;
    private String transactionId;
    private String traceId;
    private PANStatus status;
    private Boolean aadhaarLinked;
    private LocalDateTime timestamp;
    private String message;
    private String errorCode;
    private String errorMessage;

    // Constructors
    public PANVerificationResponse() {}

    public PANVerificationResponse(String referenceNumber, String transactionId,
                                 PANStatus status, Boolean aadhaarLinked) {
        this.referenceNumber = referenceNumber;
        this.transactionId = transactionId;
        this.status = status;
        this.aadhaarLinked = aadhaarLinked;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public PANStatus getStatus() { return status; }
    public void setStatus(PANStatus status) { this.status = status; }

    public Boolean getAadhaarLinked() { return aadhaarLinked; }
    public void setAadhaarLinked(Boolean aadhaarLinked) { this.aadhaarLinked = aadhaarLinked; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    @Override
    public String toString() {
        return "PANVerificationResponse{" +
                "referenceNumber='" + referenceNumber + "'" +
                ", transactionId='" + transactionId + "'" +
                ", traceId='" + traceId + "'" +
                ", status=" + status +
                ", aadhaarLinked=" + aadhaarLinked +
                ", timestamp=" + timestamp +
                "}";
    }
}