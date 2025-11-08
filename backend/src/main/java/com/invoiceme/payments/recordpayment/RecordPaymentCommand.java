package com.invoiceme.payments.recordpayment;

import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentCommand {
    private UUID invoiceId;
    private Money amount;
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    private String paymentReference;
    private String notes;
    private UUID createdByUserId; // Will be set from security context
}

