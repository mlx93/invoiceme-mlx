package com.invoiceme.infrastructure.events;

import com.invoiceme.domain.events.InvoiceSentEvent;
import com.invoiceme.infrastructure.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class InvoiceSentEmailListener {
    
    private final EmailService emailService;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceSent(InvoiceSentEvent event) {
        log.info("Handling InvoiceSentEvent for invoice {}", event.getInvoiceNumber());
        
        try {
            emailService.sendInvoiceEmail(
                event.getCustomerEmail(),
                event.getInvoiceNumber(),
                event.getInvoiceId()
            );
        } catch (Exception e) {
            log.error("Failed to send invoice email for invoice {}", event.getInvoiceNumber(), e);
            // Don't throw - email failures shouldn't break the transaction
        }
    }
}

