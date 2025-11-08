package com.invoiceme.invoices.markassent;

import com.invoiceme.invoices.shared.InvoiceDto;
import com.invoiceme.domain.invoice.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarkAsSentMapper {
    InvoiceDto toDto(Invoice invoice);
}

