package com.invoiceme.recurring.listtemplates;

import com.invoiceme.recurring.shared.RecurringTemplateDto;
import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ListRecurringTemplatesMapper {
    
    @Mapping(target = "customerName", ignore = true)
    RecurringTemplateDto toDto(RecurringInvoiceTemplate template);
}


