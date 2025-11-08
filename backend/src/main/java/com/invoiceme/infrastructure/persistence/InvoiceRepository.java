package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.invoice.Invoice;
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
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, InvoiceStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Invoice i WHERE " +
           "(:status IS NULL OR i.status IN :statusList) AND " +
           "(:customerId IS NULL OR i.customerId = :customerId) AND " +
           "(:issueDateFrom IS NULL OR i.issueDate >= :issueDateFrom) AND " +
           "(:issueDateTo IS NULL OR i.issueDate <= :issueDateTo) AND " +
           "(:dueDateFrom IS NULL OR i.dueDate >= :dueDateFrom) AND " +
           "(:dueDateTo IS NULL OR i.dueDate <= :dueDateTo) AND " +
           "(:amountFrom IS NULL OR i.totalAmount.amount >= :amountFrom) AND " +
           "(:amountTo IS NULL OR i.totalAmount.amount <= :amountTo) AND " +
           "(:search IS NULL OR i.invoiceNumber.value LIKE CONCAT('%', :search, '%'))")
    Page<Invoice> findByFilters(
        @Param("status") List<InvoiceStatus> statusList,
        @Param("customerId") UUID customerId,
        @Param("issueDateFrom") LocalDate issueDateFrom,
        @Param("issueDateTo") LocalDate issueDateTo,
        @Param("dueDateFrom") LocalDate dueDateFrom,
        @Param("dueDateTo") LocalDate dueDateTo,
        @Param("amountFrom") java.math.BigDecimal amountFrom,
        @Param("amountTo") java.math.BigDecimal amountTo,
        @Param("search") String search,
        Pageable pageable
    );
    
    @Query("SELECT i FROM Invoice i WHERE i.status IN ('SENT', 'OVERDUE') AND " +
           "i.dueDate < :currentDate AND i.balanceDue.amount > 0")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);
    
    // Note: Late fee logic checks overdue invoices, not nextInvoiceDate (that's for recurring templates)
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT SUM(i.totalAmount.amount) FROM Invoice i WHERE i.status = :status")
    Optional<java.math.BigDecimal> sumTotalAmountByStatus(@Param("status") InvoiceStatus status);
    
    @Query("SELECT SUM(i.totalAmount.amount) FROM Invoice i WHERE i.status = :status " +
           "AND i.issueDate >= :fromDate AND i.issueDate <= :toDate")
    Optional<java.math.BigDecimal> sumTotalAmountByStatusAndDateRange(
        @Param("status") InvoiceStatus status,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
    
    @Query("SELECT SUM(i.balanceDue.amount) FROM Invoice i WHERE i.status IN ('SENT', 'OVERDUE')")
    Optional<java.math.BigDecimal> sumOutstandingBalance();
}

