package com.invoiceme.customers.createcustomer;

import com.invoiceme.customers.shared.AddressDto;
import com.invoiceme.domain.common.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {
    
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;
    
    @Size(max = 255, message = "Contact name must not exceed 255 characters")
    private String contactName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 50, message = "Phone must not exceed 50 characters")
    private String phone;
    
    @Valid
    private AddressDto address;
    
    @NotNull(message = "Customer type is required")
    private CustomerType customerType;
}

