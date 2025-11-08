package com.invoiceme.customers.getcustomer;

import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.customers.shared.MoneyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse extends CustomerDto {
    private MoneyDto outstandingBalance;
    private Integer totalInvoices;
    private Integer unpaidInvoices;
}

