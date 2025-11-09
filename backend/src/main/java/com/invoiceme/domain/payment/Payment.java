package com.invoiceme.domain.payment;

import com.invoiceme.domain.common.*;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.events.PaymentRecordedEvent;
import com.invoiceme.domain.invoice.Invoice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends AggregateRoot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "invoice_id", nullable = false)
    private UUID invoiceId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false, precision = 19, scale = 2))
    private Money amount;
    
    @Convert(converter = com.invoiceme.infrastructure.persistence.PaymentMethodConverter.class)
    @Column(name = "payment_method", nullable = false, columnDefinition = "payment_method_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "payment_method::text",
        write = "?::payment_method_enum"
    )
    private PaymentMethod paymentMethod;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "payment_reference", length = 100)
    private String paymentReference;
    
    @Convert(converter = com.invoiceme.infrastructure.persistence.PaymentStatusConverter.class)
    @Column(name = "status", nullable = false, columnDefinition = "payment_status_enum")
    @org.hibernate.annotations.ColumnTransformer(
        read = "status::text",
        write = "?::payment_status_enum"
    )
    private PaymentStatus status;
    
    @Column(name = "created_by_user_id")
    private UUID createdByUserId;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = PaymentStatus.COMPLETED;
        }
    }
    
    // Static factory method
    public static Payment record(Invoice invoice, Customer customer, Money amount,
                                PaymentMethod method, LocalDate paymentDate,
                                UUID createdByUserId) {
        // Validate invoice status
        if (invoice.getStatus() != InvoiceStatus.SENT && invoice.getStatus() != InvoiceStatus.OVERDUE) {
            throw new IllegalStateException(
                "Cannot record payment for invoice with status: " + invoice.getStatus()
            );
        }
        
        // Validate amount
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        
        // Create payment entity
        Payment payment = new Payment();
        payment.invoiceId = invoice.getId();
        payment.customerId = customer.getId();
        payment.amount = amount;
        payment.paymentMethod = method;
        payment.paymentDate = paymentDate;
        payment.createdByUserId = createdByUserId;
        payment.status = PaymentStatus.COMPLETED;
        
        // Record payment on invoice (updates invoice balance)
        invoice.recordPayment(amount);
        
        // Calculate overpayment
        Money remainingBalance = invoice.getBalanceDue();
        Money overpaymentAmount = remainingBalance.isNegative() 
            ? remainingBalance.multiply(BigDecimal.valueOf(-1)) 
            : Money.zero();
        
        // Publish event
        payment.addDomainEvent(new PaymentRecordedEvent(
            payment.id,
            invoice.getId(),
            invoice.getInvoiceNumber().toString(),
            customer.getId(),
            customer.getCompanyName(),
            amount,
            method.name(),
            paymentDate,
            remainingBalance.isNegative() ? Money.zero() : remainingBalance,
            overpaymentAmount
        ));
        
        return payment;
    }
    
    // Static factory method for refunds
    public static Payment createRefund(Invoice invoice, Customer customer, Money refundAmount,
                                       UUID createdByUserId, String reason) {
        Payment refund = new Payment();
        refund.invoiceId = invoice.getId();
        refund.customerId = customer.getId();
        refund.amount = refundAmount; // Store as positive for tracking
        refund.paymentMethod = PaymentMethod.ACH; // Refunds typically via ACH
        refund.paymentDate = LocalDate.now();
        refund.createdByUserId = createdByUserId;
        refund.status = PaymentStatus.REFUNDED;
        refund.notes = "Refund: " + (reason != null ? reason : "");
        return refund;
    }
    
    public void updateReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    public void updateNotes(String notes) {
        this.notes = notes;
    }
}

