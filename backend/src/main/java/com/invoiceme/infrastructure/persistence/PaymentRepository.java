package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Page<Payment> findByInvoiceId(UUID invoiceId, Pageable pageable);
    
    Page<Payment> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<Payment> findByPaymentMethod(PaymentMethod paymentMethod, Pageable pageable);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Payment p WHERE " +
           "(:invoiceId IS NULL OR p.invoiceId = :invoiceId) AND " +
           "(:customerId IS NULL OR p.customerId = :customerId) AND " +
           "(:paymentDateFrom IS NULL OR p.paymentDate >= :paymentDateFrom) AND " +
           "(:paymentDateTo IS NULL OR p.paymentDate <= :paymentDateTo) AND " +
           "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Payment> findByFilters(
        @Param("invoiceId") UUID invoiceId,
        @Param("customerId") UUID customerId,
        @Param("paymentDateFrom") LocalDate paymentDateFrom,
        @Param("paymentDateTo") LocalDate paymentDateTo,
        @Param("paymentMethod") PaymentMethod paymentMethod,
        @Param("status") PaymentStatus status,
        Pageable pageable
    );
    
    @Query("SELECT SUM(p.amount.amount) FROM Payment p WHERE " +
           "p.paymentDate >= :fromDate AND p.paymentDate <= :toDate AND " +
           "p.status = 'COMPLETED'")
    java.math.BigDecimal sumPaymentsByDateRange(
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
    
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.invoiceId = :invoiceId")
    long countByInvoiceId(@Param("invoiceId") UUID invoiceId);
}

