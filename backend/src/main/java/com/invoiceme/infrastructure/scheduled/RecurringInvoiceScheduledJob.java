package com.invoiceme.infrastructure.scheduled;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.InvoiceNumber;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.domain.recurring.RecurringInvoiceTemplate;
import com.invoiceme.domain.recurring.TemplateLineItem;
import com.invoiceme.infrastructure.persistence.InvoiceNumberGenerator;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.RecurringInvoiceTemplateRepository;
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
public class RecurringInvoiceScheduledJob {
    
    private final RecurringInvoiceTemplateRepository templateRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final DomainEventPublisher eventPublisher;
    
    @Scheduled(cron = "0 0 * * * ?", zone = "America/Chicago") // 6 fields: second minute hour day month day-of-week (? = any day)
    @Transactional
    public void generateRecurringInvoices() {
        log.info("Starting recurring invoice generation job");
        
        LocalDate today = LocalDate.now();
        
        // Find all active templates where nextInvoiceDate <= today
        List<RecurringInvoiceTemplate> templates = templateRepository
            .findActiveTemplatesReadyForGeneration(today);
        
        log.info("Found {} templates ready for invoice generation", templates.size());
        
        for (RecurringInvoiceTemplate template : templates) {
            try {
                generateInvoiceFromTemplate(template, today);
            } catch (Exception e) {
                log.error("Failed to generate invoice from template {}", template.getId(), e);
                // Continue with next template - don't fail entire job
            }
        }
        
        log.info("Completed recurring invoice generation job");
    }
    
    private void generateInvoiceFromTemplate(RecurringInvoiceTemplate template, LocalDate today) {
        log.debug("Generating invoice from template {} for customer {}", 
            template.getId(), template.getCustomerId());
        
        // Generate invoice number
        InvoiceNumber invoiceNumber = invoiceNumberGenerator.generateNext();
        
        // Use template's generateInvoice method
        Invoice invoice = template.generateInvoice(invoiceNumber, today, eventPublisher);
        
        // Save invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Save template (nextInvoiceDate updated, status may have changed)
        templateRepository.save(template);
        
        // Publish domain events
        eventPublisher.publishEvents(savedInvoice);
        eventPublisher.publishEvents(template);
        
        log.info("Generated invoice {} from template {}", invoiceNumber, template.getId());
    }
}

