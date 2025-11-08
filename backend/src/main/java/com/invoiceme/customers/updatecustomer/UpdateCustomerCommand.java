package com.invoiceme.customers.updatecustomer;

import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerCommand {
    private UUID customerId;
    private String companyName;
    private String contactName;
    private String phone;
    private Address address;
    private CustomerType customerType;
    private CustomerStatus status;
}

