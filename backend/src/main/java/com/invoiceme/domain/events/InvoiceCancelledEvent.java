package com.invoiceme.domain.events;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class InvoiceCancelledEvent extends BaseDomainEvent {
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final String reason;
    private final Instant cancelledAt;
    private final String previousStatus;
    
    public InvoiceCancelledEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                                String customerName, String customerEmail, String reason,
                                String previousStatus) {
        super();
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.reason = reason;
        this.cancelledAt = Instant.now();
        this.previousStatus = previousStatus;
    }
}

