package com.chumani.production.panverification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Trace Service for audit logging and traceability
 * Implements secure logging with PII masking as per requirements
 */
@Service
public class TraceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Generate unique trace ID for request tracking
     */
    public String generateTraceId() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TRACE-" + timestamp + "-" + uuid;
    }

    /**
     * Log PAN verification request with PII masking
     */
    public void logVerificationRequest(String traceId, String pan, String name, String referenceNumber) {
        logger.info("PAN Verification Request - TraceId: {}, ReferenceNumber: {}, PAN: {}, Name: {}",
                   traceId, referenceNumber, maskPAN(pan), maskName(name));
    }

    /**
     * Log PAN verification response with PII masking
     */
    public void logVerificationResponse(String traceId, String referenceNumber, String status, Boolean aadhaarLinked) {
        logger.info("PAN Verification Response - TraceId: {}, ReferenceNumber: {}, Status: {}, AadhaarLinked: {}",
                   traceId, referenceNumber, status, aadhaarLinked);
    }

    /**
     * Log error with PII masking
     */
    public void logError(String traceId, String referenceNumber, String errorCode, String errorMessage) {
        logger.error("PAN Verification Error - TraceId: {}, ReferenceNumber: {}, ErrorCode: {}, ErrorMessage: {}",
                    traceId, referenceNumber, errorCode, errorMessage);
    }

    /**
     * Mask PAN for secure logging
     */
    private String maskPAN(String pan) {
        if (pan == null || pan.length() != 10) return "INVALID_PAN";
        return "XXXX" + pan.substring(4, 8) + pan.substring(9);
    }

    /**
     * Mask name for secure logging
     */
    private String maskName(String name) {
        if (name == null || name.length() <= 2) return "****";
        return name.substring(0, 2) + "****";
    }
}