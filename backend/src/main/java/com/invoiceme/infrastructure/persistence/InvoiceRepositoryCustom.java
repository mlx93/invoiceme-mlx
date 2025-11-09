package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.invoice.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepositoryCustom {
    
    Page<Invoice> findByFilters(
        List<InvoiceStatus> statusList,
        UUID customerId,
        LocalDate issueDateFrom,
        LocalDate issueDateTo,
        LocalDate dueDateFrom,
        LocalDate dueDateTo,
        BigDecimal amountFrom,
        BigDecimal amountTo,
        String search,
        Pageable pageable
    );
}


