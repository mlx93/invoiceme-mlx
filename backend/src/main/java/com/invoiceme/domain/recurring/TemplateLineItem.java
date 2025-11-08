package com.invoiceme.domain.recurring;

import com.invoiceme.domain.common.DiscountType;
import com.invoiceme.domain.common.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "template_line_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TemplateLineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private RecurringInvoiceTemplate template;
    
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
}

