package com.invoiceme.infrastructure.email;

import com.invoiceme.domain.common.Money;

import java.util.UUID;

public interface EmailService {
    void sendInvoiceEmail(String customerEmail, String invoiceNumber, UUID invoiceId);
    void sendPaymentConfirmation(String customerEmail, String invoiceNumber, Money amount);
    void sendPaymentCompletion(String customerEmail, String invoiceNumber, Money totalAmount);
    void sendOverdueReminder(String customerEmail, String invoiceNumber, Money lateFeeAmount, Money newBalance);
}

