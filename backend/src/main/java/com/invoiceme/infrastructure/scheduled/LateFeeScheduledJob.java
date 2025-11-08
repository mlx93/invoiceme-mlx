package com.invoiceme.infrastructure.scheduled;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LateFeeScheduledJob {
    
    private static final Money LATE_FEE_AMOUNT = Money.of(java.math.BigDecimal.valueOf(125.00));
    private static final int MAX_LATE_FEE_MONTHS = 3;
    
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")
    @Transactional
    public void applyLateFees() {
        log.info("Starting late fee application job");
        
        LocalDate today = LocalDate.now();
        
        // Find all overdue invoices (SENT or OVERDUE status, dueDate < today)
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(today);
        
        log.info("Found {} overdue invoices", overdueInvoices.size());
        
        for (Invoice invoice : overdueInvoices) {
            try {
                applyLateFeeIfNeeded(invoice, today);
            } catch (Exception e) {
                log.error("Failed to apply late fee to invoice {}", invoice.getId(), e);
                // Continue with next invoice - don't fail entire job
            }
        }
        
        log.info("Completed late fee application job");
    }
    
    private void applyLateFeeIfNeeded(Invoice invoice, LocalDate today) {
        // Check if invoice is overdue
        if (!invoice.isOverdue(today)) {
            return;
        }
        
        // Calculate months overdue
        long monthsOverdue = calculateMonthsOverdue(invoice.getDueDate(), today);
        
        // Check if late fee already applied for this month
        // We'll track this via a field or check event history
        // For now, apply late fee if monthsOverdue > 0 and <= MAX_LATE_FEE_MONTHS
        
        if (monthsOverdue > 0 && monthsOverdue <= MAX_LATE_FEE_MONTHS) {
            // Check if late fee already applied this month (simplified - would need better tracking)
            // For MVP, we'll apply late fee each month up to max
            
            // Apply late fee
            invoice.addLateFee(LATE_FEE_AMOUNT);
            
            Invoice savedInvoice = invoiceRepository.save(invoice);
            
            // Publish domain events
            eventPublisher.publishEvents(savedInvoice);
            
            log.info("Applied late fee of {} to invoice {}", LATE_FEE_AMOUNT, invoice.getInvoiceNumber());
        }
    }
    
    private long calculateMonthsOverdue(LocalDate dueDate, LocalDate today) {
        // Calculate months between dueDate and today
        long months = java.time.temporal.ChronoUnit.MONTHS.between(dueDate, today);
        return Math.max(0, months);
    }
}

