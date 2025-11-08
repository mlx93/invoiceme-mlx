package com.invoiceme.customers.updatecustomer;

import com.invoiceme.customers.shared.AddressDto;
import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCustomerRequest {
    
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;
    
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    private String contactName;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @Valid
    private AddressDto address;
    
    private CustomerType customerType;
    
    private CustomerStatus status;
}

