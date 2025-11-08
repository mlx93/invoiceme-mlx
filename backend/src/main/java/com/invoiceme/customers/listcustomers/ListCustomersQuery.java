package com.invoiceme.customers.listcustomers;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListCustomersQuery {
    private CustomerStatus status;
    private CustomerType customerType;
    private String search;
    private Boolean hasOutstandingBalance;
    private Integer page;
    private Integer size;
    private String sort;
}

