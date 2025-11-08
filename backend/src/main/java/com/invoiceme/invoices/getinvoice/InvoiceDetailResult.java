package com.invoiceme.invoices.getinvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailResult {
    private Invoice invoice;
    private List<Payment> payments;
}

