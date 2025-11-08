package com.invoiceme.customers.createcustomer;

import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.common.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerCommand {
    private String companyName;
    private String contactName;
    private Email email;
    private String phone;
    private Address address;
    private CustomerType customerType;
}

