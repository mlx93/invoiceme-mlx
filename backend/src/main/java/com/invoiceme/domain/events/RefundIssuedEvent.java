package com.invoiceme.domain.events;

import com.invoiceme.domain.common.Money;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RefundIssuedEvent extends BaseDomainEvent {
    private final UUID refundId;
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final Money refundAmount;
    private final String reason;
    private final Boolean applyAsCredit;
    private final Money newInvoiceBalance;
    private final String invoiceStatus;
    
    public RefundIssuedEvent(UUID refundId, UUID invoiceId, String invoiceNumber,
                            UUID customerId, String customerName, String customerEmail,
                            Money refundAmount, String reason, Boolean applyAsCredit,
                            Money newInvoiceBalance, String invoiceStatus) {
        super();
        this.refundId = refundId;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.applyAsCredit = applyAsCredit;
        this.newInvoiceBalance = newInvoiceBalance;
        this.invoiceStatus = invoiceStatus;
    }
}

