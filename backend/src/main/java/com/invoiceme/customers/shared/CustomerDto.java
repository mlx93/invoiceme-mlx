package com.invoiceme.customers.shared;

import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private UUID id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private Address address;
    private CustomerType customerType;
    private Money creditBalance;
    private CustomerStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}

