package com.invoiceme.infrastructure.persistence;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.customer.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerRepositoryCustom {
    
    Page<Customer> findByFilters(
        CustomerStatus status,
        CustomerType customerType,
        String search,
        Pageable pageable
    );
}


