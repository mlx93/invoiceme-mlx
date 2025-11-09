package com.invoiceme.recurring.listtemplates;

import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
import com.invoiceme.infrastructure.persistence.RecurringInvoiceTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListRecurringTemplatesHandler {
    
    private final RecurringInvoiceTemplateRepository templateRepository;
    
    public Page<RecurringInvoiceTemplate> handle(ListRecurringTemplatesQuery query) {
        Pageable pageable = PageRequest.of(
            query.getPage() != null ? query.getPage() : 0,
            query.getSize() != null ? query.getSize() : 20,
            buildSort(query.getSort())
        );
        
        return templateRepository.findByFilters(
            query.getCustomerId(),
            query.getStatus(),
            pageable
        );
    }
    
    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        String field = parts[0].trim();
        Sort.Direction direction = "desc".equalsIgnoreCase(parts[1].trim())
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        
        return Sort.by(direction, field);
    }
}


