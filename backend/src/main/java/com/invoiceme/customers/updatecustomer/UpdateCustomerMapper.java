package com.invoiceme.customers.updatecustomer;

import com.invoiceme.customers.shared.AddressDto;
import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UpdateCustomerMapper {
    
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "address", expression = "java(toAddress(request.getAddress()))")
    UpdateCustomerCommand toCommand(UUID customerId, UpdateCustomerRequest request);
    
    CustomerDto toDto(Customer customer);
    
    default Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto.getCountry() != null) {
            return Address.of(dto.getStreet(), dto.getCity(), dto.getState(), dto.getZipCode(), dto.getCountry());
        }
        return Address.of(dto.getStreet(), dto.getCity(), dto.getState(), dto.getZipCode());
    }
}

