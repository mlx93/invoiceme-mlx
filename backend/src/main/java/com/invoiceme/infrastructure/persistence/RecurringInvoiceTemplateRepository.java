package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecurringInvoiceTemplateRepository extends JpaRepository<RecurringInvoiceTemplate, UUID> {
    
    @Query("SELECT t FROM RecurringInvoiceTemplate t " +
           "WHERE t.status = 'ACTIVE' " +
           "AND t.nextInvoiceDate <= :currentDate " +
           "AND (t.endDate IS NULL OR t.endDate >= :currentDate)")
    List<RecurringInvoiceTemplate> findActiveTemplatesReadyForGeneration(@Param("currentDate") LocalDate currentDate);
}

