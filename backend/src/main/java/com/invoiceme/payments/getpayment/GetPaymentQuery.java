package com.invoiceme.payments.getpayment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentQuery {
    private UUID paymentId;
}

