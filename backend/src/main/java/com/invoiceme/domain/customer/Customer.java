package com.invoiceme.domain.customer;

import com.invoiceme.domain.common.*;
import com.invoiceme.domain.events.CreditAppliedEvent;
import com.invoiceme.domain.events.CreditDeductedEvent;
import com.invoiceme.domain.events.CustomerDeactivatedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer extends AggregateRoot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;
    
    @Column(name = "contact_name", length = 255)
    private String contactName;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    private Email email;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Embedded
    private Address address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "credit_balance", nullable = false, precision = 19, scale = 2))
    private Money creditBalance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        if (creditBalance == null) {
            creditBalance = Money.zero();
        }
        if (status == null) {
            status = CustomerStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    // Factory method
    public static Customer create(String companyName, Email email, CustomerType customerType) {
        Customer customer = new Customer();
        customer.companyName = companyName;
        customer.email = email;
        customer.customerType = customerType;
        customer.creditBalance = Money.zero();
        customer.status = CustomerStatus.ACTIVE;
        return customer;
    }
    
    // Behavior methods
    
    public void applyCredit(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        Money previousBalance = this.creditBalance;
        this.creditBalance = this.creditBalance.add(amount);
        
        addDomainEvent(new CreditAppliedEvent(
            this.id,
            this.companyName,
            amount,
            previousBalance,
            this.creditBalance,
            "Manual Credit"
        ));
    }
    
    public void deductCredit(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        
        if (creditBalance.isLessThan(amount)) {
            throw new IllegalStateException("Insufficient credit balance");
        }
        
        Money previousBalance = this.creditBalance;
        this.creditBalance = this.creditBalance.subtract(amount);
        
        addDomainEvent(new CreditDeductedEvent(
            this.id,
            this.companyName,
            amount,
            previousBalance,
            this.creditBalance,
            null, // invoiceId - will be set by caller
            null  // invoiceNumber - will be set by caller
        ));
    }
    
    public boolean canBeDeleted() {
        // Business rule: Customer can be deleted only if:
        // - All invoices are paid (balance = $0) OR cancelled
        // - No active recurring invoice templates
        // - Credit balance = $0.00
        // Note: This method should be called with context from repository
        // For now, we check credit balance only
        return creditBalance.isZero() && status != CustomerStatus.INACTIVE;
    }
    
    public void markAsInactive() {
        if (!canBeDeleted()) {
            throw new IllegalStateException("Customer cannot be deleted. Outstanding balance or active templates exist.");
        }
        
        this.status = CustomerStatus.INACTIVE;
        
        addDomainEvent(new CustomerDeactivatedEvent(
            this.id,
            this.companyName,
            "Zero balance, all invoices paid"
        ));
    }
    
    public void update(String companyName, String contactName, String phone, Address address, CustomerType customerType) {
        if (companyName != null && !companyName.trim().isEmpty()) {
            this.companyName = companyName;
        }
        if (contactName != null) {
            this.contactName = contactName;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (address != null) {
            this.address = address;
        }
        if (customerType != null) {
            this.customerType = customerType;
        }
    }
}

