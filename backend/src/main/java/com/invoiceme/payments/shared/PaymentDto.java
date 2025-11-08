package com.invoiceme.payments.shared;

import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private UUID id;
    private UUID invoiceId;
    private String invoiceNumber;
    private UUID customerId;
    private String customerName;
    private Money amount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String paymentReference;
    private String status;
    private Instant createdAt;
}

