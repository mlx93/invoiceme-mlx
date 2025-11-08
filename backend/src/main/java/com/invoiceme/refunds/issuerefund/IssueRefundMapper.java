package com.invoiceme.refunds.issuerefund;

import com.invoiceme.domain.common.Money;
import com.invoiceme.payments.shared.PaymentDto;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IssueRefundMapper {
    
    @Mapping(target = "amount", expression = "java(Money.of(request.getAmount()))")
    IssueRefundCommand requestToCommand(IssueRefundRequest request);
    
    PaymentDto toDto(Payment payment);
}

