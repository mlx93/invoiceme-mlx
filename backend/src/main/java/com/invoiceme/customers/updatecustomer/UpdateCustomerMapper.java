package com.invoiceme.customers.updatecustomer;

import com.invoiceme.customers.shared.AddressDto;
import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.domain.common.Address;
import com.invoiceme.domain.common.Email;
import com.invoiceme.domain.customer.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UpdateCustomerMapper {
    
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "address", expression = "java(toAddress(request.getAddress()))")
    UpdateCustomerCommand toCommand(UUID customerId, UpdateCustomerRequest request);
    
    CustomerDto toDto(Customer customer);
    
    default String map(Email email) {
        return email == null ? null : email.getValue();
    }
    
    default Address toAddress(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        // Only create Address if all required fields are present and non-empty
        // Address is optional - if fields are incomplete, return null
        if (dto.getStreet() == null || dto.getStreet().trim().isEmpty() ||
            dto.getCity() == null || dto.getCity().trim().isEmpty() ||
            dto.getState() == null || dto.getState().trim().isEmpty() ||
            dto.getZipCode() == null || dto.getZipCode().trim().isEmpty()) {
            return null;
        }
        if (dto.getCountry() != null) {
            return Address.of(dto.getStreet(), dto.getCity(), dto.getState(), dto.getZipCode(), dto.getCountry());
        }
        return Address.of(dto.getStreet(), dto.getCity(), dto.getState(), dto.getZipCode());
    }
}

