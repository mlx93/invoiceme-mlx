package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class PaymentRecordedEvent extends BaseDomainEvent {
    private final UUID paymentId;
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final Money amount;
    private final String paymentMethod;
    private final LocalDate paymentDate;
    private final Money remainingBalance;
    private final Money overpaymentAmount;
    
    public PaymentRecordedEvent(UUID paymentId, UUID invoiceId, String invoiceNumber,
                               UUID customerId, String customerName, Money amount,
                               String paymentMethod, LocalDate paymentDate,
                               Money remainingBalance, Money overpaymentAmount) {
        super();
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
        this.remainingBalance = remainingBalance;
        this.overpaymentAmount = overpaymentAmount;
    }
}

