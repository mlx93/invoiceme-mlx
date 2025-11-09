package com.invoiceme.customers.reactivatecustomer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactivateCustomerCommand {
    private UUID customerId;
}

