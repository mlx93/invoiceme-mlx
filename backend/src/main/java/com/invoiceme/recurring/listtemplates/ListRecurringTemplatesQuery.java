package com.invoiceme.recurring.listtemplates;

import com.invoiceme.domain.common.TemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListRecurringTemplatesQuery {
    private UUID customerId;
    private TemplateStatus status;
    private Integer page;
    private Integer size;
    private String sort;
}


