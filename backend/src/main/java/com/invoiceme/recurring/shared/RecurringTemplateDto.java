package com.invoiceme.recurring.shared;

import com.invoiceme.domain.common.Frequency;
import com.invoiceme.domain.common.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTemplateDto {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String templateName;
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextInvoiceDate;
    private TemplateStatus status;
    private boolean autoSend;
    private Instant createdAt;
}


