package com.invoiceme.invoices.listinvoices;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListInvoicesHandler {
    
    private final InvoiceRepository invoiceRepository;
    
    public Page<Invoice> handle(ListInvoicesQuery query) {
        // Build pagination
        Sort sort = buildSort(query.getSort());
        Pageable pageable = PageRequest.of(
            query.getPage() != null ? query.getPage() : 0,
            query.getSize() != null ? query.getSize() : 20,
            sort
        );
        
        // Use repository filter method
        return invoiceRepository.findByFilters(
            query.getStatus(),
            query.getCustomerId(),
            query.getIssueDateFrom(),
            query.getIssueDateTo(),
            query.getDueDateFrom(),
            query.getDueDateTo(),
            query.getAmountFrom(),
            query.getAmountTo(),
            query.getSearch(),
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

