package com.invoiceme.customers.getcustomer;

import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.customers.shared.MoneyDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse extends CustomerDto {
    private MoneyDto outstandingBalance;
    private Integer totalInvoices;
    private Integer unpaidInvoices;
}

