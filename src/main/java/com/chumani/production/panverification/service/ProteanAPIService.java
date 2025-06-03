package com.chumani.production.panverification.service;

import com.chumani.production.panverification.dto.PANVerificationResponse;
import com.chumani.production.panverification.enums.PANStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Protean API Service
 * Implements business scenarios: even digit = Active, odd digit = Inactive
 * Includes retry logic and error handling as per requirements
 */
@Service
public class ProteanAPIService {

    private static final Logger logger = LoggerFactory.getLogger(ProteanAPIService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    @Value("${protean.api.url:http://localhost:8082/api/pan/internal/v1/verify}")
    private String proteanApiUrl;

    @Value("${protean.api.timeout:5000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;

    public ProteanAPIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Verify PAN with retry logic
     */
    public PANVerificationResponse verifyPANWithRetry(String pan, String name, String traceId) {
        int attempt = 1;
        Exception lastException = null;

        while (attempt <= MAX_RETRY_ATTEMPTS) {
            try {
                logger.info("PAN verification attempt {} - TraceId: {}, PAN: {}",
                           attempt, traceId, maskPAN(pan));

                PANVerificationResponse response = verifyPAN(pan, name, traceId);

                logger.info("PAN verification successful on attempt {} - TraceId: {}, Status: {}",
                           attempt, traceId, response.getStatus());

                return response;

            } catch (Exception e) {
                lastException = e;
                logger.warn("PAN verification failed on attempt {} - TraceId: {}, Error: {}",
                           attempt, traceId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
                attempt++;
            }
        }

        logger.error("PAN verification failed after {} attempts - TraceId: {}",
                    MAX_RETRY_ATTEMPTS, traceId);

        // Create error response
        PANVerificationResponse errorResponse = new PANVerificationResponse();
        errorResponse.setErrorCode("API_FAILURE");
        errorResponse.setErrorMessage("Failed after " + MAX_RETRY_ATTEMPTS + " attempts: " +
                                    (lastException != null ? lastException.getMessage() : "Unknown error"));
        errorResponse.setTimestamp(LocalDateTime.now());

        return errorResponse;
    }

    /**
     * Single PAN verification attempt
     * Implements business logic from prompts:
     * - Even digit ending = Active
     * - Odd digit ending = Inactive
     * - ZZZ prefix = Error simulation
     * - Ending with 9 = Delay simulation
     */
    private PANVerificationResponse verifyPAN(String pan, String name, String traceId) {

        // Business scenario: ZZZ prefix causes error (from mock.txt)
        if (pan.startsWith("ZZZ")) {
            logger.info("Simulating error for PAN starting with ZZZ - TraceId: {}", traceId);
            throw new RuntimeException("Simulated system error for PAN starting with ZZZ");
        }

        // Business scenario: Ending with 9 causes delay (from mock.txt)
        if (pan.endsWith("9")) {
            logger.info("Simulating delay for PAN ending with 9 - TraceId: {}", traceId);
            try {
                Thread.sleep(5000); // 5 second delay as per requirements
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Delay simulation interrupted", e);
            }
        }

        // Extract last digit for business logic
        char lastDigit = pan.charAt(pan.length() - 1);

        PANVerificationResponse response = new PANVerificationResponse();
        response.setTransactionId("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        response.setTraceId(traceId);
        response.setTimestamp(LocalDateTime.now());

        // Business logic from prompts: Even digit = Active, Odd digit = Inactive
        if (Character.isDigit(lastDigit)) {
            int digit = Character.getNumericValue(lastDigit);
            if (digit % 2 == 0) {
                // Even digit = Active
                response.setStatus(PANStatus.ACTIVE);
                response.setAadhaarLinked(true);
                logger.info("PAN ends with even digit ({}), setting status to ACTIVE - TraceId: {}",
                           digit, traceId);
            } else {
                // Odd digit = Inactive
                response.setStatus(PANStatus.INACTIVE);
                response.setAadhaarLinked(false);
                logger.info("PAN ends with odd digit ({}), setting status to INACTIVE - TraceId: {}",
                           digit, traceId);
            }
        } else {
            // Non-digit ending (shouldn't happen with proper validation)
            response.setStatus(PANStatus.INACTIVE);
            response.setAadhaarLinked(false);
            logger.warn("PAN ends with non-digit character, setting status to INACTIVE - TraceId: {}", traceId);
        }

        return response;
    }

    private String maskPAN(String pan) {
        if (pan == null || pan.length() != 10) return "INVALID_PAN";
        return "XXXX" + pan.substring(4, 8) + pan.substring(9);
    }
}