package com.invoiceme.infrastructure.events;

import com.invoiceme.domain.events.PaymentRecordedEvent;
import com.invoiceme.domain.events.InvoiceSentEvent;
import com.invoiceme.domain.events.RecurringInvoiceGeneratedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class DashboardCacheInvalidationListener {
    
    private final CacheManager cacheManager;
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentRecorded(PaymentRecordedEvent event) {
        log.debug("Invalidating dashboard cache due to PaymentRecordedEvent");
        invalidateCache();
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceSent(InvoiceSentEvent event) {
        log.debug("Invalidating dashboard cache due to InvoiceSentEvent");
        invalidateCache();
    }
    
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecurringInvoiceGenerated(RecurringInvoiceGeneratedEvent event) {
        log.debug("Invalidating dashboard cache due to RecurringInvoiceGeneratedEvent");
        invalidateCache();
    }
    
    private void invalidateCache() {
        try {
            var dashboardCache = cacheManager.getCache("dashboardMetrics");
            if (dashboardCache != null) {
                dashboardCache.clear();
                log.debug("Dashboard cache invalidated");
            }
        } catch (Exception e) {
            log.error("Failed to invalidate dashboard cache", e);
            // Don't throw - cache invalidation failures shouldn't break the transaction
        }
    }
}

