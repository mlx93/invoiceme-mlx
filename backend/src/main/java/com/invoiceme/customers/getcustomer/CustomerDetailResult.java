package com.invoiceme.customers.getcustomer;

import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.customer.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResult {
    private Customer customer;
    private Money outstandingBalance;
    private Integer totalInvoices;
    private Integer unpaidInvoices;
}

