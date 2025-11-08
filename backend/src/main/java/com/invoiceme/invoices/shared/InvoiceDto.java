package com.invoiceme.invoices.shared;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentTerms;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private PaymentTerms paymentTerms;
    private Money totalAmount;
    private Money amountPaid;
    private Money balanceDue;
    private Instant createdAt;
}

