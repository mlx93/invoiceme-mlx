package com.invoiceme.domain.events;

import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class RecurringInvoiceGeneratedEvent extends BaseDomainEvent {
    private final UUID templateId;
    private final String templateName;
    private final UUID invoiceId;
    private final String invoiceNumber;
    private final UUID customerId;
    private final String customerName;
    private final String customerEmail;
    private final LocalDate nextInvoiceDate;
    private final Boolean autoSend;
    private final LocalDate generatedDate;
    
    public RecurringInvoiceGeneratedEvent(UUID templateId, String templateName, UUID invoiceId,
                                         String invoiceNumber, UUID customerId, String customerName,
                                         String customerEmail, LocalDate nextInvoiceDate,
                                         Boolean autoSend, LocalDate generatedDate) {
        super();
        this.templateId = templateId;
        this.templateName = templateName;
        this.invoiceId = invoiceId;
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.nextInvoiceDate = nextInvoiceDate;
        this.autoSend = autoSend;
        this.generatedDate = generatedDate;
    }
}

