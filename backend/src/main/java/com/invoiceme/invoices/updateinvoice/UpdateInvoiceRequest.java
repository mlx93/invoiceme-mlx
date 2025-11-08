package com.invoiceme.invoices.updateinvoice;

import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.invoices.shared.LineItemRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceRequest {
    private LocalDate issueDate;
    private LocalDate dueDate;
    private PaymentTerms paymentTerms;
    private List<LineItemRequestDto> lineItems;
    private String notes;
    @Min(value = 1, message = "Version is required for optimistic locking")
    private Integer version;
}

