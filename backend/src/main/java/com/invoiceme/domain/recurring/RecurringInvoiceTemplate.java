package com.invoiceme.domain.recurring;

import com.invoiceme.domain.common.AggregateRoot;
import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.Frequency;
import com.invoiceme.domain.common.InvoiceNumber;
import com.invoiceme.domain.common.PaymentTerms;
import com.invoiceme.domain.common.TemplateStatus;
import com.invoiceme.domain.events.RecurringInvoiceGeneratedEvent;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.invoice.LineItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recurring_invoice_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecurringInvoiceTemplate extends AggregateRoot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "template_name", nullable = false, length = 255)
    private String templateName;
    
    @Convert(converter = com.invoiceme.infrastructure.persistence.FrequencyConverter.class)
    @Column(name = "frequency", nullable = false, columnDefinition = "frequency_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "frequency::text",
        write = "?::frequency_enum"
    )
    private Frequency frequency;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "next_invoice_date", nullable = false)
    private LocalDate nextInvoiceDate;
    
    @Convert(converter = com.invoiceme.infrastructure.persistence.TemplateStatusConverter.class)
    @Column(name = "status", nullable = false, columnDefinition = "template_status_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "status::text",
        write = "?::template_status_enum"
    )
    private TemplateStatus status;
    
    @Convert(converter = com.invoiceme.infrastructure.persistence.PaymentTermsConverter.class)
    @Column(name = "payment_terms", nullable = false, columnDefinition = "payment_terms_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "payment_terms::text",
        write = "?::payment_terms_enum"
    )
    private PaymentTerms paymentTerms;
    
    @Column(name = "auto_send", nullable = false)
    private boolean autoSend;
    
    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<TemplateLineItem> lineItems = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Behavior methods
    
    public Invoice generateInvoice(InvoiceNumber invoiceNumber, LocalDate issueDate, DomainEventPublisher eventPublisher) {
        if (status != TemplateStatus.ACTIVE) {
            throw new IllegalStateException("Cannot generate invoice from " + status + " template");
        }
        
        if (issueDate.isBefore(nextInvoiceDate)) {
            throw new IllegalArgumentException("Issue date must be >= nextInvoiceDate");
        }
        
        // Calculate due date
        LocalDate dueDate = calculateDueDate(issueDate);
        
        // Create invoice
        Invoice invoice = Invoice.create(
            customerId,
            invoiceNumber,
            issueDate,
            dueDate,
            paymentTerms
        );
        
        // Copy template line items to invoice line items
        int sortOrder = 0;
        for (TemplateLineItem templateItem : lineItems) {
            LineItem lineItem = LineItem.create(
                templateItem.getDescription(),
                templateItem.getQuantity(),
                templateItem.getUnitPrice(),
                templateItem.getDiscountType(),
                templateItem.getDiscountValue(),
                templateItem.getTaxRate(),
                sortOrder++
            );
            invoice.addLineItem(lineItem);
        }
        
        // Auto-send if enabled
        if (autoSend) {
            invoice.markAsSent();
        }
        
        // Update next invoice date
        LocalDate nextDate = calculateNextDate(issueDate);
        this.nextInvoiceDate = nextDate;
        
        // Check if template is completed
        if (endDate != null && nextDate != null && nextDate.isAfter(endDate)) {
            this.status = TemplateStatus.COMPLETED;
            this.nextInvoiceDate = null;
        }
        
        // Publish event (will be published after transaction commit)
        addDomainEvent(new RecurringInvoiceGeneratedEvent(
            id,
            templateName,
            invoice.getId(),
            invoice.getInvoiceNumber().toString(),
            customerId,
            null, // customerName - will be set by listener
            null, // customerEmail - will be set by listener
            nextInvoiceDate,
            autoSend,
            issueDate
        ));
        
        return invoice;
    }
    
    public void pause() {
        if (status != TemplateStatus.ACTIVE) {
            throw new IllegalStateException("Can only pause ACTIVE template");
        }
        this.status = TemplateStatus.PAUSED;
        this.nextInvoiceDate = null;
    }
    
    public void resume() {
        if (status != TemplateStatus.PAUSED) {
            throw new IllegalStateException("Can only resume PAUSED template");
        }
        this.status = TemplateStatus.ACTIVE;
        this.nextInvoiceDate = calculateNextDate(LocalDate.now());
    }
    
    public void complete() {
        if (status == TemplateStatus.COMPLETED) {
            throw new IllegalStateException("Template already completed");
        }
        this.status = TemplateStatus.COMPLETED;
        this.endDate = LocalDate.now();
        this.nextInvoiceDate = null;
    }
    
    public LocalDate calculateNextDate(LocalDate currentDate) {
        if (endDate != null && currentDate.isAfter(endDate)) {
            return null; // Template completed
        }
        
        return switch (frequency) {
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case ANNUALLY -> currentDate.plusYears(1);
        };
    }
    
    private LocalDate calculateDueDate(LocalDate issueDate) {
        return switch (paymentTerms) {
            case NET_30 -> issueDate.plusDays(30);
            case DUE_ON_RECEIPT -> issueDate;
            case CUSTOM -> issueDate.plusDays(30); // Default
        };
    }
}

