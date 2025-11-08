package com.invoiceme.customers.listcustomers;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListCustomersHandler {
    
    private final CustomerRepository customerRepository;
    
    public Page<Customer> handle(ListCustomersQuery query) {
        // Build pagination
        Sort sort = buildSort(query.getSort());
        Pageable pageable = PageRequest.of(
            query.getPage() != null ? query.getPage() : 0,
            query.getSize() != null ? query.getSize() : 20,
            sort
        );
        
        // Handle outstanding balance filter
        if (Boolean.TRUE.equals(query.getHasOutstandingBalance())) {
            return customerRepository.findCustomersWithOutstandingBalance(pageable);
        }
        
        // Use repository filter method
        return customerRepository.findByFilters(
            query.getStatus(),
            query.getCustomerType(),
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

