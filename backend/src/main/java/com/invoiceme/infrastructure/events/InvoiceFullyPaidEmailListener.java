package com.invoiceme.infrastructure.events;

import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.events.InvoiceFullyPaidEvent;
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
public class InvoiceFullyPaidEmailListener {
    
    private final EmailService emailService;
    private final CustomerRepository customerRepository;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceFullyPaid(InvoiceFullyPaidEvent event) {
        log.info("Handling InvoiceFullyPaidEvent for invoice {}", event.getInvoiceNumber());
        
        try {
            Customer customer = customerRepository.findById(event.getCustomerId())
                .orElse(null);
            
            if (customer != null && customer.getEmail() != null) {
                emailService.sendPaymentCompletion(
                    customer.getEmail().getValue(),
                    event.getInvoiceNumber(),
                    event.getTotalAmount()
                );
            } else {
                log.warn("Customer not found or email missing for invoice {}", event.getInvoiceNumber());
            }
        } catch (Exception e) {
            log.error("Failed to send payment completion email for invoice {}", event.getInvoiceNumber(), e);
        }
    }
}

