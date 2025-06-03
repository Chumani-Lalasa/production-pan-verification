package com.chumani.production.panverification.service;

import com.chumani.production.panverification.dto.PANVerificationRequest;
import com.chumani.production.panverification.dto.PANVerificationResponse;
import com.chumani.production.panverification.entity.PANVerificationRecord;
import com.chumani.production.panverification.enums.PANStatus;
import com.chumani.production.panverification.repository.PANVerificationRepository;
import com.chumani.production.panverification.service.ProteanAPIService;
import com.chumani.production.panverification.service.TraceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PAN Verification Service
 * Implements comprehensive business logic as per requirements
 */
@Service
@Transactional
public class PANVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(PANVerificationService.class);

    @Autowired
    private PANVerificationRepository repository;

    @Autowired
    private ProteanAPIService proteanAPIService;

    @Autowired
    private TraceService traceService;

    /**
     * Main PAN verification method with comprehensive business logic
     */
    public PANVerificationResponse verifyPAN(PANVerificationRequest request) {
        String traceId = traceService.generateTraceId();
        String referenceNumber = generateReferenceNumber();
        String transactionId = generateTransactionId();

        logger.info("Starting PAN verification - TraceId: {}, ReferenceNumber: {}, PAN: {}",
                   traceId, referenceNumber, request.getMaskedPan());

        try {
            // Create initial record
            PANVerificationRecord record = new PANVerificationRecord(
                request.getPan(),
                request.getName(),
                PANStatus.ACTIVE, // Default, will be updated
                false, // Default, will be updated
                referenceNumber,
                transactionId
            );
            record.setTraceId(traceId);
            record.setRequestTimestamp(LocalDateTime.now());

            // Save initial record
            record = repository.save(record);

            // Call Protean API with retry logic
            PANVerificationResponse apiResponse = proteanAPIService.verifyPANWithRetry(
                request.getPan(), request.getName(), traceId
            );

            // Update record with API response
            record.setStatus(apiResponse.getStatus());
            record.setAadhaarLinked(apiResponse.getAadhaarLinked());
            record.setResponseTimestamp(LocalDateTime.now());
            record.setErrorCode(apiResponse.getErrorCode());
            record.setErrorMessage(apiResponse.getErrorMessage());

            // Save updated record
            repository.save(record);

            // Create response
            PANVerificationResponse response = new PANVerificationResponse(
                referenceNumber, transactionId, apiResponse.getStatus(), apiResponse.getAadhaarLinked()
            );
            response.setTraceId(traceId);
            response.setMessage("PAN verification completed successfully");

            logger.info("PAN verification completed - TraceId: {}, Status: {}, AadhaarLinked: {}",
                       traceId, apiResponse.getStatus(), apiResponse.getAadhaarLinked());

            return response;

        } catch (Exception e) {
            logger.error("PAN verification failed - TraceId: {}, Error: {}", traceId, e.getMessage(), e);

            // Update record with error
            Optional<PANVerificationRecord> recordOpt = repository.findByReferenceNumber(referenceNumber);
            if (recordOpt.isPresent()) {
                PANVerificationRecord record = recordOpt.get();
                record.setErrorCode("VERIFICATION_FAILED");
                record.setErrorMessage(e.getMessage());
                record.setResponseTimestamp(LocalDateTime.now());
                repository.save(record);
            }

            // Create error response
            PANVerificationResponse response = new PANVerificationResponse();
            response.setReferenceNumber(referenceNumber);
            response.setTransactionId(transactionId);
            response.setTraceId(traceId);
            response.setErrorCode("VERIFICATION_FAILED");
            response.setErrorMessage("PAN verification failed: " + e.getMessage());
            response.setTimestamp(LocalDateTime.now());

            return response;
        }
    }

    /**
     * Get verification status by reference number
     */
    public Optional<PANVerificationResponse> getVerificationStatus(String referenceNumber) {
        logger.info("Retrieving verification status for reference: {}", referenceNumber);

        return repository.findByReferenceNumber(referenceNumber)
            .map(this::convertToResponse);
    }

    /**
     * Get verification history for a PAN
     */
    public List<PANVerificationResponse> getVerificationHistory(String panNumber) {
        logger.info("Retrieving verification history for PAN: {}", maskPAN(panNumber));

        return repository.findPANHistory(panNumber)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get recent verifications (last 24 hours)
     */
    public List<PANVerificationResponse> getRecentVerifications() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return repository.findRecentVerifications(yesterday)
            .stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get verification statistics
     */
    public VerificationStats getVerificationStats() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        Long activeCount = repository.countByStatusSince(PANStatus.ACTIVE, yesterday);
        Long inactiveCount = repository.countByStatusSince(PANStatus.INACTIVE, yesterday);
        Long errorCount = repository.findByErrorCodeIsNotNull().stream()
            .filter(r -> r.getCreatedAt().isAfter(yesterday))
            .count();

        return new VerificationStats(activeCount, inactiveCount, errorCount);
    }

    // Helper methods
    private String generateReferenceNumber() {
        return "PAN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String generateTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String maskPAN(String pan) {
        if (pan == null || pan.length() != 10) return "INVALID_PAN";
        return "XXXX" + pan.substring(4, 8) + pan.substring(9);
    }

    private PANVerificationResponse convertToResponse(PANVerificationRecord record) {
        PANVerificationResponse response = new PANVerificationResponse();
        response.setReferenceNumber(record.getReferenceNumber());
        response.setTransactionId(record.getTransactionId());
        response.setTraceId(record.getTraceId());
        response.setStatus(record.getStatus());
        response.setAadhaarLinked(record.getAadhaarLinked());
        response.setTimestamp(record.getCreatedAt());
        response.setErrorCode(record.getErrorCode());
        response.setErrorMessage(record.getErrorMessage());

        if (record.getErrorCode() == null) {
            response.setMessage("PAN verification completed successfully");
        } else {
            response.setMessage("PAN verification failed");
        }

        return response;
    }

    /**
     * Inner class for verification statistics
     */
    public static class VerificationStats {
        private final Long activeCount;
        private final Long inactiveCount;
        private final Long errorCount;

        public VerificationStats(Long activeCount, Long inactiveCount, Long errorCount) {
            this.activeCount = activeCount;
            this.inactiveCount = inactiveCount;
            this.errorCount = errorCount;
        }

        public Long getActiveCount() { return activeCount; }
        public Long getInactiveCount() { return inactiveCount; }
        public Long getErrorCount() { return errorCount; }
        public Long getTotalCount() { return activeCount + inactiveCount + errorCount; }
    }
}