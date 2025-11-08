package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.util.UUID;

@Getter
public class LateFeeAppliedEvent extends BaseDomainEvent {
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final Money lateFeeAmount;
    private final Money newBalance;
    private final Integer daysOverdue;
    private final String month;
    
    public LateFeeAppliedEvent(UUID invoiceId, String invoiceNumber, UUID customerId,
                              String customerName, String customerEmail, Money lateFeeAmount,
                              Money newBalance, Integer daysOverdue, String month) {
        super();
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.lateFeeAmount = lateFeeAmount;
        this.newBalance = newBalance;
        this.daysOverdue = daysOverdue;
        this.month = month;
    }
}

