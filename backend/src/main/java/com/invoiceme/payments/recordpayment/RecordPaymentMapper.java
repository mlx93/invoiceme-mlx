package com.invoiceme.payments.recordpayment;

import com.invoiceme.domain.common.Money;
import com.invoiceme.payments.shared.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordPaymentMapper {
    
    @Mapping(target = "amount", expression = "java(Money.of(request.getAmount()))")
    RecordPaymentCommand requestToCommand(RecordPaymentRequest request);
    
    PaymentDto toDto(Payment payment);
}

