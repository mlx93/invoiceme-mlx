package com.invoiceme.infrastructure.events;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.events.PaymentRecordedEvent;
import com.invoiceme.infrastructure.email.EmailService;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentRecordedEmailListener {
    
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentRecorded(PaymentRecordedEvent event) {
        log.info("Handling PaymentRecordedEvent for payment {}", event.getPaymentId());
        
        try {
            Customer customer = customerRepository.findById(event.getCustomerId())
                .orElse(null);
            
            if (customer != null && customer.getEmail() != null) {
                emailService.sendPaymentConfirmation(
                    customer.getEmail().getValue(),
                    event.getInvoiceNumber(),
                    event.getAmount()
                );
            } else {
                log.warn("Customer not found or email missing for payment {}", event.getPaymentId());
            }
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email for invoice {}", event.getInvoiceNumber(), e);
        }
    }
}

