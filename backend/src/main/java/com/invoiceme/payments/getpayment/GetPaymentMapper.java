package com.invoiceme.payments.getpayment;

import com.invoiceme.payments.shared.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GetPaymentMapper {
    PaymentDto toDto(Payment payment);
}

