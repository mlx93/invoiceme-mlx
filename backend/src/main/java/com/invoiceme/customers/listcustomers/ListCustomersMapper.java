package com.invoiceme.customers.listcustomers;

import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.domain.customer.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListCustomersMapper {
    CustomerDto toDto(Customer customer);
}

