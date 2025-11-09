package com.invoiceme.invoices.updateinvoice;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateInvoiceHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Invoice handle(UpdateInvoiceCommand command) {
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.getInvoiceId()));
        
        // Optimistic locking check
        if (command.getVersion() != null && !invoice.getVersion().equals(command.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(
                "Invoice was modified by another transaction. Current version: " + invoice.getVersion(),
                Invoice.class
            );
        }
        
        // Business rules: Draft vs Sent status
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            // DRAFT: All fields editable
            if (command.getIssueDate() != null || command.getDueDate() != null) {
                invoice.updateDates(command.getIssueDate(), command.getDueDate());
            }
            if (command.getPaymentTerms() != null) {
                invoice.updatePaymentTerms(command.getPaymentTerms());
            }
            
            // Update line items
            if (command.getLineItems() != null && !command.getLineItems().isEmpty()) {
                // Capture existing items BEFORE making changes (only items with IDs - persisted items)
                var existingItemIds = invoice.getLineItems().stream()
                    .filter(item -> item.getId() != null)
                    .map(LineItem::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                // Add new line items FIRST
                for (LineItem lineItem : command.getLineItems()) {
                    invoice.addLineItem(lineItem);
                }
                
                // Then remove all OLD line items (only the persisted ones we captured)
                for (java.util.UUID itemId : existingItemIds) {
                    invoice.removeLineItem(itemId);
                }
            }
            
            if (command.getNotes() != null) {
                invoice.updateNotes(command.getNotes());
            }
        } else if (invoice.getStatus() == InvoiceStatus.SENT) {
            // SENT: Only line items editable (with version tracking)
            if (command.getLineItems() != null && !command.getLineItems().isEmpty()) {
                // Capture existing items BEFORE making changes (only items with IDs - persisted items)
                var existingItemIds = invoice.getLineItems().stream()
                    .filter(item -> item.getId() != null)
                    .map(LineItem::getId)
                    .collect(java.util.stream.Collectors.toList());
                
                // Add new line items FIRST
                for (LineItem lineItem : command.getLineItems()) {
                    invoice.addLineItem(lineItem);
                }
                
                // Then remove all OLD line items (only the persisted ones we captured)
                for (java.util.UUID itemId : existingItemIds) {
                    invoice.removeLineItem(itemId);
                }
            }
        } else {
            // PAID, CANCELLED, OVERDUE: No changes allowed
            throw new IllegalStateException("Cannot update invoice with status: " + invoice.getStatus());
        }
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedInvoice);
        
        return savedInvoice;
    }
}

