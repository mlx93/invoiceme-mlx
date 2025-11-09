package com.invoiceme.customers;

import com.invoiceme.customers.createcustomer.*;
import com.invoiceme.customers.deletecustomer.DeleteCustomerCommand;
import com.invoiceme.customers.deletecustomer.DeleteCustomerHandler;
import com.invoiceme.customers.getcustomer.*;
import com.invoiceme.customers.listcustomers.*;
import com.invoiceme.customers.reactivatecustomer.ReactivateCustomerCommand;
import com.invoiceme.customers.reactivatecustomer.ReactivateCustomerHandler;
import com.invoiceme.customers.shared.CustomerDto;
import com.invoiceme.customers.updatecustomer.*;
import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.customer.Customer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    // Create Customer
    private final CreateCustomerHandler createHandler;
    private final CreateCustomerMapper createMapper;
    private final CreateCustomerValidator createValidator;
    
    // Get Customer
    private final GetCustomerHandler getHandler;
    
    // List Customers
    private final ListCustomersHandler listHandler;
    
    // Update Customer
    private final UpdateCustomerHandler updateHandler;
    private final UpdateCustomerMapper updateMapper;
    private final UpdateCustomerValidator updateValidator;
    
    // Delete Customer
    private final DeleteCustomerHandler deleteHandler;
    
    // Reactivate Customer
    private final ReactivateCustomerHandler reactivateHandler;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        createValidator.validate(request);
        CreateCustomerCommand command = createMapper.requestToCommand(request);
        var customer = createHandler.handle(command);
        CustomerDto response = createMapper.toDto(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDetailResponse> getCustomer(@PathVariable UUID id) {
        GetCustomerQuery query = new GetCustomerQuery(id);
        CustomerDetailResult result = getHandler.handle(query);
        
        CustomerDetailResponse response = CustomerDetailResponse.builder()
            .id(result.getCustomer().getId())
            .companyName(result.getCustomer().getCompanyName())
            .contactName(result.getCustomer().getContactName())
            .email(result.getCustomer().getEmail().getValue())
            .phone(result.getCustomer().getPhone())
            .address(result.getCustomer().getAddress())
            .customerType(result.getCustomer().getCustomerType())
            .creditBalance(result.getCustomer().getCreditBalance())
            .status(result.getCustomer().getStatus())
            .createdAt(result.getCustomer().getCreatedAt())
            .updatedAt(result.getCustomer().getUpdatedAt())
            .outstandingBalance(com.invoiceme.customers.shared.MoneyDto.builder()
                .amount(result.getOutstandingBalance().getAmount())
                .currency(result.getOutstandingBalance().getCurrency())
                .build())
            .totalInvoices(result.getTotalInvoices())
            .unpaidInvoices(result.getUnpaidInvoices())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<PagedCustomerResponse> listCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean hasOutstandingBalance,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String sort) {
        
        ListCustomersQuery query = ListCustomersQuery.builder()
            .status(status)
            .customerType(customerType)
            .search(search)
            .hasOutstandingBalance(hasOutstandingBalance)
            .page(page)
            .size(size)
            .sort(sort)
            .build();
        
        Page<com.invoiceme.domain.customer.Customer> customerPage = listHandler.handle(query);
        
        PagedCustomerResponse response = PagedCustomerResponse.builder()
            .content(customerPage.getContent().stream()
                .map(customer -> {
                    CustomerDto dto = new CustomerDto();
                    dto.setId(customer.getId());
                    dto.setCompanyName(customer.getCompanyName());
                    dto.setContactName(customer.getContactName());
                    dto.setEmail(customer.getEmail().getValue());
                    dto.setPhone(customer.getPhone());
                    dto.setAddress(customer.getAddress());
                    dto.setCustomerType(customer.getCustomerType());
                    dto.setCreditBalance(customer.getCreditBalance());
                    dto.setStatus(customer.getStatus());
                    dto.setCreatedAt(customer.getCreatedAt());
                    dto.setUpdatedAt(customer.getUpdatedAt());
                    return dto;
                })
                .toList())
            .page(customerPage.getNumber())
            .size(customerPage.getSize())
            .totalElements(customerPage.getTotalElements())
            .totalPages(customerPage.getTotalPages())
            .first(customerPage.isFirst())
            .last(customerPage.isLast())
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request) {
        
        updateValidator.validate(id, request);
        UpdateCustomerCommand command = updateMapper.toCommand(id, request);
        var customer = updateHandler.handle(command);
        CustomerDto response = updateMapper.toDto(customer);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable UUID id) {
        DeleteCustomerCommand command = new DeleteCustomerCommand(id);
        deleteHandler.handle(command);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<CustomerDto> reactivateCustomer(@PathVariable UUID id) {
        ReactivateCustomerCommand command = new ReactivateCustomerCommand(id);
        Customer customer = reactivateHandler.handle(command);
        
        CustomerDto response = new CustomerDto();
        response.setId(customer.getId());
        response.setCompanyName(customer.getCompanyName());
        response.setContactName(customer.getContactName());
        response.setEmail(customer.getEmail().getValue());
        response.setPhone(customer.getPhone());
        response.setAddress(customer.getAddress());
        response.setCustomerType(customer.getCustomerType());
        response.setCreditBalance(customer.getCreditBalance());
        response.setStatus(customer.getStatus());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        
        return ResponseEntity.ok(response);
    }
}

