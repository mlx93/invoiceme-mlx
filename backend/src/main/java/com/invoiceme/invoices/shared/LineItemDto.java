package com.invoiceme.invoices.shared;

import com.invoiceme.domain.common.DiscountType;
import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineItemDto {
    private UUID id;
    private String description;
    private Integer quantity;
    private Money unitPrice;
    private DiscountType discountType;
    private Money discountValue;
    private BigDecimal taxRate;
    private Money lineTotal;
    private Integer sortOrder;
}

