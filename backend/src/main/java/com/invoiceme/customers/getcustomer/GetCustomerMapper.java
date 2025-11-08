package com.invoiceme.customers.getcustomer;

import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.domain.customer.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GetCustomerMapper {
    CustomerDto toDto(Customer customer);
}

