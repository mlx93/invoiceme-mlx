package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class InvoiceSentEvent extends BaseDomainEvent {
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final Money totalAmount;
    private final LocalDate dueDate;
    private final LocalDate issueDate;
    private final Integer lineItemsCount;
    private final Money creditApplied;
    
    public InvoiceSentEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                           String customerName, String customerEmail, Money totalAmount,
                           LocalDate dueDate, LocalDate issueDate, Integer lineItemsCount,
                           Money creditApplied) {
        super();
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalAmount = totalAmount;
        this.dueDate = dueDate;
        this.issueDate = issueDate;
        this.lineItemsCount = lineItemsCount;
        this.creditApplied = creditApplied;
    }
}

