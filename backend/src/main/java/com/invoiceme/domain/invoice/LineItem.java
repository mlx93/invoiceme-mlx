package com.invoiceme.domain.invoice;

import com.invoiceme.domain.common.DiscountType;
import com.invoiceme.domain.common.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "line_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false, precision = 19, scale = 2))
    private Money unitPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;
    
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "discount_value", nullable = false, precision = 10, scale = 2))
    private Money discountValue;
    
    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
    
    public static LineItem create(String description, Integer quantity, Money unitPrice,
                                 DiscountType discountType, Money discountValue,
                                 BigDecimal taxRate, Integer sortOrder) {
        LineItem item = new LineItem();
        item.description = description;
        item.quantity = quantity;
        item.unitPrice = unitPrice;
        item.discountType = discountType;
        item.discountValue = discountValue != null ? discountValue : Money.zero();
        item.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        item.sortOrder = sortOrder != null ? sortOrder : 0;
        return item;
    }
    
    public Money calculateLineTotal() {
        // Base Amount = Quantity × Unit Price
        Money baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        
        // Discount Amount
        Money discountAmount;
        if (discountType == DiscountType.PERCENTAGE) {
            BigDecimal discountPercent = discountValue.getAmount().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discountAmount = baseAmount.multiply(discountPercent);
        } else if (discountType == DiscountType.FIXED) {
            discountAmount = discountValue;
            if (discountAmount.isGreaterThan(baseAmount)) {
                discountAmount = baseAmount; // Cap discount at base amount
            }
        } else {
            discountAmount = Money.zero();
        }
        
        // Taxable Amount = Base - Discount
        Money taxableAmount = baseAmount.subtract(discountAmount);
        
        // Tax Amount = Taxable × (Tax Rate / 100)
        BigDecimal taxMultiplier = taxRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        Money taxAmount = taxableAmount.multiply(taxMultiplier);
        
        // Line Total = Taxable + Tax
        return taxableAmount.add(taxAmount);
    }
    
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}

