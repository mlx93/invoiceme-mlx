package com.invoiceme.invoices.markassent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAsSentCommand {
    private UUID invoiceId;
}

