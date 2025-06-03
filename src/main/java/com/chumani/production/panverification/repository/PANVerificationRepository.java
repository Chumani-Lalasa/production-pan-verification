package com.chumani.production.panverification.repository;

import com.chumani.production.panverification.entity.PANVerificationRecord;
import com.chumani.production.panverification.enums.PANStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PAN Verification Records
 * Provides comprehensive data access methods
 */
@Repository
public interface PANVerificationRepository extends JpaRepository<PANVerificationRecord, Long> {

    // Basic finders
    Optional<PANVerificationRecord> findByReferenceNumber(String referenceNumber);
    Optional<PANVerificationRecord> findByTransactionId(String transactionId);
    List<PANVerificationRecord> findByPanNumber(String panNumber);
    List<PANVerificationRecord> findByTraceId(String traceId);

    // Status-based queries
    List<PANVerificationRecord> findByStatus(PANStatus status);
    List<PANVerificationRecord> findByPanNumberAndStatus(String panNumber, PANStatus status);

    // Date range queries
    List<PANVerificationRecord> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<PANVerificationRecord> findByRequestTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Error tracking
    List<PANVerificationRecord> findByErrorCodeIsNotNull();
    List<PANVerificationRecord> findByRetryCountGreaterThan(Integer retryCount);

    // Audit queries
    @Query("SELECT p FROM PANVerificationRecord p WHERE p.panNumber = :panNumber ORDER BY p.createdAt DESC")
    List<PANVerificationRecord> findPANHistory(@Param("panNumber") String panNumber);

    @Query("SELECT p FROM PANVerificationRecord p WHERE p.createdAt >= :fromDate ORDER BY p.createdAt DESC")
    List<PANVerificationRecord> findRecentVerifications(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(p) FROM PANVerificationRecord p WHERE p.status = :status AND p.createdAt >= :fromDate")
    Long countByStatusSince(@Param("status") PANStatus status, @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT p FROM PANVerificationRecord p WHERE p.responseTimestamp IS NULL")
    List<PANVerificationRecord> findPendingVerifications();

    // Existence checks
    boolean existsByReferenceNumber(String referenceNumber);
    boolean existsByTransactionId(String transactionId);
    boolean existsByPanNumberAndCreatedAtAfter(String panNumber, LocalDateTime afterDate);

    // Custom business queries
    @Query("SELECT p FROM PANVerificationRecord p WHERE p.panNumber = :panNumber AND p.status = 'ACTIVE' ORDER BY p.createdAt DESC")
    Optional<PANVerificationRecord> findLatestActivePAN(@Param("panNumber") String panNumber);

    @Query("SELECT DISTINCT p.panNumber FROM PANVerificationRecord p WHERE p.createdAt >= :fromDate")
    List<String> findDistinctPANsVerifiedSince(@Param("fromDate") LocalDateTime fromDate);
}