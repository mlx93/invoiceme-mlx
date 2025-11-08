package com.invoiceme.payments.listpayments;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
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
public class ListPaymentsQuery {
    private UUID invoiceId;
    private UUID customerId;
    private LocalDate paymentDateFrom;
    private LocalDate paymentDateTo;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Integer page;
    private Integer size;
    private String sort;
}

