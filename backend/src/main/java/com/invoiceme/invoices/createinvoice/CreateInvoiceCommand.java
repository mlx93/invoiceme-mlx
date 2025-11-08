package com.invoiceme.invoices.createinvoice;

import com.invoiceme.domain.common.InvoiceNumber;
import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.domain.invoice.LineItem;
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
public class CreateInvoiceCommand {
    private UUID customerId;
    private InvoiceNumber invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private PaymentTerms paymentTerms;
    private List<LineItem> lineItems;
    private String notes;
}

