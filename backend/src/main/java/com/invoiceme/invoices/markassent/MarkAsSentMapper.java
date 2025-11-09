package com.invoiceme.invoices.markassent;

import com.invoiceme.domain.common.InvoiceNumber;
import com.invoiceme.invoices.shared.InvoiceDto;
import com.invoiceme.domain.invoice.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarkAsSentMapper {
    InvoiceDto toDto(Invoice invoice);
    
    default String map(InvoiceNumber invoiceNumber) {
        return invoiceNumber == null ? null : invoiceNumber.getValue();
    }
}

