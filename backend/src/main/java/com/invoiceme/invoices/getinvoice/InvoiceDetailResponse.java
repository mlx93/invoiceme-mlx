package com.invoiceme.invoices.getinvoice;

import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.invoices.shared.LineItemDto;
import com.invoiceme.invoices.shared.PaymentSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String status;
    private PaymentTerms paymentTerms;
    private List<LineItemDto> lineItems;
    private Money subtotal;
    private Money taxAmount;
    private Money discountAmount;
    private Money totalAmount;
    private Money amountPaid;
    private Money balanceDue;
    private String notes;
    private Instant sentDate;
    private Instant paidDate;
    private List<PaymentSummaryDto> payments;
    private String pdfUrl; // Will be generated later
}

