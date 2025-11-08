package com.invoiceme.invoices.getinvoice;

import com.invoiceme.invoices.shared.LineItemDto;
import com.invoiceme.invoices.shared.PaymentSummaryDto;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.domain.payment.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GetInvoiceMapper {
    LineItemDto toLineItemDto(LineItem lineItem);
    PaymentSummaryDto toPaymentSummaryDto(Payment payment);
    List<LineItemDto> toLineItemDtoList(List<LineItem> lineItems);
    List<PaymentSummaryDto> toPaymentSummaryDtoList(List<Payment> payments);
}

