package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.TemplateStatus;
import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RecurringInvoiceTemplateRepositoryCustom {
    
    Page<RecurringInvoiceTemplate> findByFilters(
        UUID customerId,
        TemplateStatus status,
        Pageable pageable
    );
}


