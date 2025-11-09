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
public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, InvoiceRepositoryCustom {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Page<Invoice> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable);
    
    Page<Invoice> findByCustomerIdAndStatus(UUID customerId, InvoiceStatus status, Pageable pageable);
    
    Page<Invoice> findByFilters(
        List<InvoiceStatus> statusList,
        UUID customerId,
        LocalDate issueDateFrom,
        LocalDate issueDateTo,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        java.math.BigDecimal amountFrom,
        java.math.BigDecimal amountTo,
        String search,
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

