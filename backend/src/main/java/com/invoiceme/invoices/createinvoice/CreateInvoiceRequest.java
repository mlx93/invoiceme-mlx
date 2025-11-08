package com.invoiceme.invoices.createinvoice;

import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.invoices.shared.LineItemRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    
    @NotNull(message = "Customer ID is required")
    private UUID customerId;
    
    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;
    
    private LocalDate dueDate; // Optional, calculated if not provided
    
    @NotNull(message = "Payment terms is required")
    private PaymentTerms paymentTerms;
    
    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<LineItemRequestDto> lineItems;
    
    private String notes;
}

