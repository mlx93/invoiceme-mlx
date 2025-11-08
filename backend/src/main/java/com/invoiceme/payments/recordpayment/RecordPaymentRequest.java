package com.invoiceme.payments.recordpayment;

import com.invoiceme.domain.common.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordPaymentRequest {
    
    @NotNull(message = "Invoice ID is required")
    private UUID invoiceId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    @Size(max = 100, message = "Payment reference must not exceed 100 characters")
    private String paymentReference;
    
    private String notes;
}

