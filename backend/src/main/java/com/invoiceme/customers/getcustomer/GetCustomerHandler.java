package com.invoiceme.customers.getcustomer;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCustomerHandler {
    
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    
    public CustomerDetailResult handle(GetCustomerQuery query) {
        Customer customer = customerRepository.findById(query.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + query.getCustomerId()));
        
        // Calculate outstanding balance and invoice counts
        var customerInvoices = invoiceRepository.findByCustomerId(customer.getId(), 
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE));
        
        var outstandingBalance = customerInvoices.getContent().stream()
            .filter(inv -> inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.SENT || 
                          inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.OVERDUE)
            .map(inv -> inv.getBalanceDue().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        var totalInvoices = customerInvoices.getTotalElements();
        var unpaidInvoices = (int) customerInvoices.getContent().stream()
            .filter(inv -> inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.SENT || 
                          inv.getStatus() == com.invoiceme.domain.common.InvoiceStatus.OVERDUE)
            .count();
        
        return CustomerDetailResult.builder()
            .customer(customer)
            .outstandingBalance(outstandingBalance != null ? com.invoiceme.domain.common.Money.of(outstandingBalance) : com.invoiceme.domain.common.Money.zero())
            .totalInvoices((int) totalInvoices)
            .unpaidInvoices((int) unpaidInvoices)
            .build();
    }
}

