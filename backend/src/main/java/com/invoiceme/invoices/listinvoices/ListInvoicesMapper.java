package com.invoiceme.invoices.listinvoices;

import com.invoiceme.invoices.shared.InvoiceDto;
import com.invoiceme.domain.invoice.Invoice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListInvoicesMapper {
    InvoiceDto toDto(Invoice invoice);
}

