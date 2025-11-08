package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreditDeductedEvent extends BaseDomainEvent {
    private final UUID customerId;
    private final String customerName;
    private final Money amount;
    private final Money previousBalance;
    private final Money newBalance;
    private final UUID invoiceId;
    private final String invoiceNumber;
    
    public CreditDeductedEvent(UUID customerId, String customerName, Money amount,
                              Money previousBalance, Money newBalance, UUID invoiceId,
                              String invoiceNumber) {
        super();
        this.customerId = customerId;
        this.customerName = customerName;
        this.amount = amount;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
    }
}

