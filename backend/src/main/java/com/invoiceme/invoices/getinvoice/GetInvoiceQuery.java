package com.invoiceme.invoices.getinvoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetInvoiceQuery {
    private UUID invoiceId;
}

