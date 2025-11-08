package com.invoiceme.invoices.shared;

import com.invoiceme.domain.common.Money;
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
public class PaymentSummaryDto {
    private UUID id;
    private Money amount;
    private String paymentMethod;
    private LocalDate paymentDate;
    private String status;
}

