package com.invoiceme.invoices.updateinvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.invoices.shared.InvoiceDto;
import com.invoiceme.invoices.shared.LineItemRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UpdateInvoiceMapper {
    
    @Mapping(target = "invoiceId", source = "invoiceId")
    @Mapping(target = "lineItems", expression = "java(toLineItems(request.getLineItems()))")
    UpdateInvoiceCommand toCommand(UUID invoiceId, UpdateInvoiceRequest request);
    
    InvoiceDto toDto(Invoice invoice);
    
    default List<LineItem> toLineItems(List<LineItemRequestDto> dtos) {
        if (dtos == null) {
            return null;
        }
        java.util.List<LineItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            LineItemRequestDto dto = dtos.get(i);
            items.add(LineItem.create(
                dto.getDescription(),
                dto.getQuantity(),
                com.invoiceme.domain.common.Money.of(dto.getUnitPrice()),
                dto.getDiscountType() != null ? dto.getDiscountType() : com.invoiceme.domain.common.DiscountType.NONE,
                dto.getDiscountValue() != null ? com.invoiceme.domain.common.Money.of(dto.getDiscountValue()) : com.invoiceme.domain.common.Money.zero(),
                dto.getTaxRate() != null ? dto.getTaxRate() : java.math.BigDecimal.ZERO,
                i
            ));
        }
        return items;
    }
}

