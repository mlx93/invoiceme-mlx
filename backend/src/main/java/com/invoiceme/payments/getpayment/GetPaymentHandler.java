package com.invoiceme.payments.getpayment;

import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPaymentHandler {
    
    private final PaymentRepository paymentRepository;
    
    public Payment handle(GetPaymentQuery query) {
        return paymentRepository.findById(query.getPaymentId())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + query.getPaymentId()));
    }
}

