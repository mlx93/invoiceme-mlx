package com.invoiceme.invoices.listinvoices;

import com.invoiceme.domain.common.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListInvoicesQuery {
    private List<InvoiceStatus> status;
    private UUID customerId;
    private LocalDate issueDateFrom;
    private LocalDate issueDateTo;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private java.math.BigDecimal amountFrom;
    private java.math.BigDecimal amountTo;
    private String search;
    private Integer page;
    private Integer size;
    private String sort;
}

