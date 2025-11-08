package com.invoiceme.domain.invoice;

import com.invoiceme.domain.common.*;
import com.invoiceme.domain.events.InvoiceCancelledEvent;
import com.invoiceme.domain.events.InvoiceFullyPaidEvent;
import com.invoiceme.domain.events.InvoiceSentEvent;
import com.invoiceme.domain.events.LateFeeAppliedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invoice extends AggregateRoot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "invoice_number", nullable = false, unique = true, length = 20))
    private InvoiceNumber invoiceNumber;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;
    
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_terms", nullable = false)
    private PaymentTerms paymentTerms;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<LineItem> lineItems = new ArrayList<>();
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "subtotal", nullable = false, precision = 19, scale = 2))
    private Money subtotal;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2))
    private Money taxAmount;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2))
    private Money discountAmount;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_amount", nullable = false, precision = 19, scale = 2))
    private Money totalAmount;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount_paid", nullable = false, precision = 19, scale = 2))
    private Money amountPaid;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance_due", nullable = false, precision = 19, scale = 2))
    private Money balanceDue;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "sent_date")
    private Instant sentDate;
    
    @Column(name = "paid_date")
    private Instant paidDate;
    
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (subtotal == null) {
            subtotal = Money.zero();
        }
        if (taxAmount == null) {
            taxAmount = Money.zero();
        }
        if (discountAmount == null) {
            discountAmount = Money.zero();
        }
        if (totalAmount == null) {
            totalAmount = Money.zero();
        }
        if (amountPaid == null) {
            amountPaid = Money.zero();
        }
        if (balanceDue == null) {
            balanceDue = Money.zero();
        }
        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }
        if (version == null) {
            version = 1;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Factory method
    public static Invoice create(UUID customerId, InvoiceNumber invoiceNumber, LocalDate issueDate,
                                LocalDate dueDate, PaymentTerms paymentTerms) {
        Invoice invoice = new Invoice();
        invoice.customerId = customerId;
        invoice.invoiceNumber = invoiceNumber;
        invoice.issueDate = issueDate;
        invoice.dueDate = dueDate;
        invoice.paymentTerms = paymentTerms;
        invoice.status = InvoiceStatus.DRAFT;
        invoice.subtotal = Money.zero();
        invoice.taxAmount = Money.zero();
        invoice.discountAmount = Money.zero();
        invoice.totalAmount = Money.zero();
        invoice.amountPaid = Money.zero();
        invoice.balanceDue = Money.zero();
        invoice.version = 1;
        return invoice;
    }
    
    // Behavior methods
    
    public void addLineItem(LineItem lineItem) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot add line items to " + status + " invoice");
        }
        
        lineItem.setInvoice(this);
        lineItems.add(lineItem);
        recalculateTotals();
        version++;
    }
    
    public void removeLineItem(UUID lineItemId) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot remove line items from " + status + " invoice");
        }
        
        boolean removed = lineItems.removeIf(item -> item.getId().equals(lineItemId));
        if (!removed) {
            throw new IllegalArgumentException("Line item not found: " + lineItemId);
        }
        
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Invoice must have at least one line item");
        }
        
        recalculateTotals();
        version++;
    }
    
    public void markAsSent() {
        if (status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Can only mark DRAFT invoices as sent. Current status: " + status);
        }
        
        if (lineItems.isEmpty()) {
            throw new IllegalStateException("Cannot mark invoice as sent without line items");
        }
        
        this.status = InvoiceStatus.SENT;
        this.sentDate = Instant.now();
        
        // Note: Credit auto-application would be handled by command handler
        // This method publishes the event, and the handler checks customer credit
        
        addDomainEvent(new InvoiceSentEvent(
            this.id,
            this.invoiceNumber.toString(),
            this.customerId,
            null, // customerName - will be set by handler
            null, // customerEmail - will be set by handler
            this.totalAmount,
            this.dueDate,
            this.issueDate,
            this.lineItems.size(),
            Money.zero() // creditApplied - will be set by handler if credit applied
        ));
    }
    
    public void recordPayment(Money amount) {
        if (status != InvoiceStatus.SENT && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Can only record payment for SENT or OVERDUE invoices. Current status: " + status);
        }
        
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        Money previousBalance = this.balanceDue;
        this.amountPaid = this.amountPaid.add(amount);
        
        // Update balance due
        this.balanceDue = this.totalAmount.subtract(this.amountPaid);
        
        // If fully paid, update status
        if (this.balanceDue.isZero() || this.balanceDue.isNegative()) {
            this.status = InvoiceStatus.PAID;
            this.paidDate = Instant.now();
            this.balanceDue = Money.zero(); // Ensure non-negative
            
            // Publish InvoiceFullyPaidEvent
            addDomainEvent(new InvoiceFullyPaidEvent(
                this.id,
                this.invoiceNumber.toString(),
                this.customerId,
                this.totalAmount
            ));
        }
    }
    
    public void recordRefund(Money refundAmount) {
        if (status != InvoiceStatus.PAID) {
            throw new IllegalStateException("Can only issue refund for PAID invoices. Current status: " + status);
        }
        
        if (refundAmount == null || !refundAmount.isPositive()) {
            throw new IllegalArgumentException("Refund amount must be positive");
        }
        
        if (refundAmount.isGreaterThan(this.amountPaid)) {
            throw new IllegalArgumentException("Refund amount cannot exceed amount paid");
        }
        
        // Reduce amount paid
        this.amountPaid = this.amountPaid.subtract(refundAmount);
        
        // Update balance due
        this.balanceDue = this.totalAmount.subtract(this.amountPaid);
        
        // If partial refund, change status from PAID → SENT
        if (this.balanceDue.isPositive()) {
            this.status = InvoiceStatus.SENT;
            this.paidDate = null; // Clear paid date
        }
    }
    
    public void applyCreditDiscount(Money creditAmount) {
        if (status != InvoiceStatus.DRAFT && status != InvoiceStatus.SENT) {
            throw new IllegalStateException("Can only apply credit to DRAFT or SENT invoices");
        }
        
        if (creditAmount == null || !creditAmount.isPositive()) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        // Create discount line item
        LineItem creditLineItem = LineItem.create(
            "Credit Applied - " + creditAmount.toString(),
            1,
            creditAmount,
            DiscountType.FIXED,
            creditAmount,
            BigDecimal.ZERO,
            lineItems.size() // Add at end
        );
        
        addLineItem(creditLineItem);
        // recalculateTotals() is called by addLineItem
    }
    
    public void addLateFee(Money lateFeeAmount) {
        if (status != InvoiceStatus.SENT && status != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException("Can only add late fee to SENT or OVERDUE invoices");
        }
        
        if (lateFeeAmount == null || !lateFeeAmount.isPositive()) {
            throw new IllegalArgumentException("Late fee amount must be positive");
        }
        
        // Check if late fee already exists for current month
        String currentMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy").format(java.time.LocalDate.now());
        boolean lateFeeExists = lineItems.stream()
            .anyMatch(item -> item.getDescription().startsWith("Late Fee - " + currentMonth));
        
        if (lateFeeExists) {
            throw new IllegalStateException("Late fee already applied for " + currentMonth);
        }
        
        // Create late fee line item
        LineItem lateFeeLineItem = LineItem.create(
            "Late Fee - " + currentMonth,
            1,
            lateFeeAmount,
            DiscountType.NONE,
            Money.zero(),
            BigDecimal.ZERO,
            lineItems.size()
        );
        
        addLineItem(lateFeeLineItem);
        
        // Update status to OVERDUE if not already
        if (status == InvoiceStatus.SENT) {
            this.status = InvoiceStatus.OVERDUE;
        }
        
        addDomainEvent(new LateFeeAppliedEvent(
            this.id,
            this.invoiceNumber.toString(),
            this.customerId,
            null, // customerEmail - will be set by handler
            lateFeeAmount,
            this.balanceDue,
            calculateDaysOverdue(),
            currentMonth
        ));
    }
    
    public void cancel() {
        if (status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Cannot cancel PAID invoices. Must issue refund instead.");
        }
        
        if (status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Invoice is already cancelled");
        }
        
        if (!amountPaid.isZero()) {
            throw new IllegalStateException("Cannot cancel invoice with payments. Must issue refund first.");
        }
        
        String previousStatus = this.status.name();
        this.status = InvoiceStatus.CANCELLED;
        
        addDomainEvent(new InvoiceCancelledEvent(
            this.id,
            this.invoiceNumber.toString(),
            this.customerId,
            null, // customerName - will be set by handler
            null, // customerEmail - will be set by handler
            "Invoice cancelled",
            previousStatus
        ));
    }
    
    public boolean isOverdue() {
        if (status != InvoiceStatus.SENT && status != InvoiceStatus.OVERDUE) {
            return false;
        }
        
        if (balanceDue.isZero() || balanceDue.isNegative()) {
            return false;
        }
        
        return LocalDate.now().isAfter(dueDate);
    }
    
    private int calculateDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }
    
    private void recalculateTotals() {
        Money calculatedSubtotal = Money.zero();
        Money calculatedTaxAmount = Money.zero();
        Money calculatedDiscountAmount = Money.zero();
        
        for (LineItem item : lineItems) {
            Money lineTotal = item.calculateLineTotal();
            
            // Calculate base amount (before discount and tax)
            Money baseAmount = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            
            // Calculate discount
            Money itemDiscount;
            if (item.getDiscountType() == DiscountType.PERCENTAGE) {
                BigDecimal discountPercent = item.getDiscountValue().getAmount()
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                itemDiscount = baseAmount.multiply(discountPercent);
            } else if (item.getDiscountType() == DiscountType.FIXED) {
                itemDiscount = item.getDiscountValue();
                if (itemDiscount.isGreaterThan(baseAmount)) {
                    itemDiscount = baseAmount;
                }
            } else {
                itemDiscount = Money.zero();
            }
            
            calculatedDiscountAmount = calculatedDiscountAmount.add(itemDiscount);
            
            // Calculate tax
            Money taxableAmount = baseAmount.subtract(itemDiscount);
            BigDecimal taxMultiplier = item.getTaxRate()
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            Money itemTax = taxableAmount.multiply(taxMultiplier);
            calculatedTaxAmount = calculatedTaxAmount.add(itemTax);
            
            calculatedSubtotal = calculatedSubtotal.add(baseAmount);
        }
        
        this.subtotal = calculatedSubtotal;
        this.taxAmount = calculatedTaxAmount;
        this.discountAmount = calculatedDiscountAmount;
        this.totalAmount = calculatedSubtotal.add(calculatedTaxAmount).subtract(calculatedDiscountAmount);
        this.balanceDue = this.totalAmount.subtract(this.amountPaid);
        
        // Ensure non-negative values
        if (this.balanceDue.isNegative()) {
            this.balanceDue = Money.zero();
        }
    }
    
    public void updateNotes(String notes) {
        this.notes = notes;
    }
    
    public void updateDates(LocalDate issueDate, LocalDate dueDate) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update dates for " + status + " invoice");
        }
        if (issueDate != null) {
            this.issueDate = issueDate;
        }
        if (dueDate != null) {
            if (issueDate != null && dueDate.isBefore(issueDate)) {
                throw new IllegalArgumentException("Due date must be >= issue date");
            }
            this.dueDate = dueDate;
        }
    }
    
    public void updatePaymentTerms(PaymentTerms paymentTerms) {
        if (status == InvoiceStatus.PAID || status == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update payment terms for " + status + " invoice");
        }
        if (paymentTerms != null) {
            this.paymentTerms = paymentTerms;
        }
    }
}

