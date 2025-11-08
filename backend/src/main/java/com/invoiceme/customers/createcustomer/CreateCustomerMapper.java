package com.invoiceme.customers.createcustomer;

import com.invoiceme.customers.shared.AddressDto;
import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.Email;
import com.invoiceme.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreateCustomerMapper {
    
    @Mapping(target = "email", expression = "java(Email.of(request.getEmail()))")
    @Mapping(target = "address", expression = "java(toAddress(request.getAddress()))")
    CreateCustomerCommand requestToCommand(CreateCustomerRequest request);
    
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

