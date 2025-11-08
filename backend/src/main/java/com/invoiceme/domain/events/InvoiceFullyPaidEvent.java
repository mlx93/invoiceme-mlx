package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class InvoiceFullyPaidEvent extends BaseDomainEvent {
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final Money totalAmount;
    private final Instant paidDate;
    private final Integer paymentCount;
    
    public InvoiceFullyPaidEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                                 String customerName, Money totalAmount, Instant paidDate,
                                 Integer paymentCount) {
        super();
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.paidDate = paidDate;
        this.paymentCount = paymentCount;
    }
}

