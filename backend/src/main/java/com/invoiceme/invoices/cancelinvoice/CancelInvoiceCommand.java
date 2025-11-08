package com.invoiceme.invoices.cancelinvoice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelInvoiceCommand {
    private UUID invoiceId;
}

