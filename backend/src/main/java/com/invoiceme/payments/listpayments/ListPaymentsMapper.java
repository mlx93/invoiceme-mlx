package com.invoiceme.payments.listpayments;

import com.invoiceme.payments.shared.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListPaymentsMapper {
    PaymentDto toDto(Payment payment);
}

