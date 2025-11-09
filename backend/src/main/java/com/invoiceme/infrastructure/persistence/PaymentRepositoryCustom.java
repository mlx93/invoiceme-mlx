package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentStatus;
import com.invoiceme.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface PaymentRepositoryCustom {
    
    Page<Payment> findByFilters(
        UUID invoiceId,
        UUID customerId,
        LocalDate paymentDateFrom,
        LocalDate paymentDateTo,
        PaymentMethod paymentMethod,
        PaymentStatus status,
        Pageable pageable
    );
}


