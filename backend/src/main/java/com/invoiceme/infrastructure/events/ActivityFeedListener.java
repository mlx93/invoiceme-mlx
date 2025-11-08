package com.invoiceme.infrastructure.events;

import com.invoiceme.domain.common.DomainEvent;
import com.invoiceme.domain.events.*;
import com.invoiceme.infrastructure.persistence.ActivityFeed;
import com.invoiceme.infrastructure.persistence.ActivityFeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityFeedListener {
    
    private final ActivityFeedRepository activityFeedRepository;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDomainEvent(DomainEvent event) {
        log.debug("Logging domain event to activity feed: {}", event.getEventType());
        
        try {
            ActivityFeed entry = createActivityFeedEntry(event);
            activityFeedRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to log event to activity feed: {}", event.getEventType(), e);
            // Don't throw - activity feed failures shouldn't break the transaction
        }
    }
    
    private ActivityFeed createActivityFeedEntry(DomainEvent event) {
        String description = generateDescription(event);
        UUID aggregateId = getAggregateId(event);
        
        return ActivityFeed.create(
            aggregateId,
            event.getEventType(),
            description,
            null // userId - will be set from security context if available
        );
    }
    
    private UUID getAggregateId(DomainEvent event) {
        if (event instanceof PaymentRecordedEvent) {
            return ((PaymentRecordedEvent) event).getInvoiceId();
        } else if (event instanceof InvoiceSentEvent) {
            return ((InvoiceSentEvent) event).getInvoiceId();
        } else if (event instanceof InvoiceFullyPaidEvent) {
            return ((InvoiceFullyPaidEvent) event).getInvoiceId();
        } else if (event instanceof LateFeeAppliedEvent) {
            return ((LateFeeAppliedEvent) event).getInvoiceId();
        } else if (event instanceof InvoiceCancelledEvent) {
            return ((InvoiceCancelledEvent) event).getInvoiceId();
        } else if (event instanceof CreditAppliedEvent) {
            return ((CreditAppliedEvent) event).getCustomerId();
        } else if (event instanceof CreditDeductedEvent) {
            return ((CreditDeductedEvent) event).getCustomerId();
        } else if (event instanceof CustomerDeactivatedEvent) {
            return ((CustomerDeactivatedEvent) event).getCustomerId();
        } else if (event instanceof RecurringInvoiceGeneratedEvent) {
            return ((RecurringInvoiceGeneratedEvent) event).getInvoiceId();
        } else if (event instanceof RefundIssuedEvent) {
            return ((RefundIssuedEvent) event).getInvoiceId();
        }
        return null;
    }
    
    private String generateDescription(DomainEvent event) {
        if (event instanceof PaymentRecordedEvent) {
            PaymentRecordedEvent e = (PaymentRecordedEvent) event;
            return String.format("Payment of %s recorded for invoice %s", e.getAmount(), e.getInvoiceNumber());
        } else if (event instanceof InvoiceSentEvent) {
            InvoiceSentEvent e = (InvoiceSentEvent) event;
            return String.format("Invoice %s sent to customer", e.getInvoiceNumber());
        } else if (event instanceof InvoiceFullyPaidEvent) {
            InvoiceFullyPaidEvent e = (InvoiceFullyPaidEvent) event;
            return String.format("Invoice %s paid in full", e.getInvoiceNumber());
        } else if (event instanceof LateFeeAppliedEvent) {
            LateFeeAppliedEvent e = (LateFeeAppliedEvent) event;
            return String.format("Late fee of %s applied to invoice %s", e.getLateFeeAmount(), e.getInvoiceNumber());
        } else if (event instanceof InvoiceCancelledEvent) {
            InvoiceCancelledEvent e = (InvoiceCancelledEvent) event;
            return String.format("Invoice %s cancelled", e.getInvoiceNumber());
        } else if (event instanceof CreditAppliedEvent) {
            CreditAppliedEvent e = (CreditAppliedEvent) event;
            return String.format("Credit of %s applied to customer (Source: %s)", e.getAmount(), e.getSource());
        } else if (event instanceof CreditDeductedEvent) {
            CreditDeductedEvent e = (CreditDeductedEvent) event;
            return String.format("Credit of %s deducted from customer for invoice %s", e.getAmount(), e.getInvoiceNumber());
        } else if (event instanceof CustomerDeactivatedEvent) {
            CustomerDeactivatedEvent e = (CustomerDeactivatedEvent) event;
            return String.format("Customer %s deactivated: %s", e.getCustomerName(), e.getReason());
        } else if (event instanceof RecurringInvoiceGeneratedEvent) {
            RecurringInvoiceGeneratedEvent e = (RecurringInvoiceGeneratedEvent) event;
            return String.format("Recurring invoice %s generated from template %s", e.getInvoiceNumber(), e.getTemplateName());
        } else if (event instanceof RefundIssuedEvent) {
            RefundIssuedEvent e = (RefundIssuedEvent) event;
            return String.format("Refund of %s issued for invoice %s", e.getRefundAmount(), e.getInvoiceNumber());
        }
        return "Domain event: " + event.getEventType();
    }
}

