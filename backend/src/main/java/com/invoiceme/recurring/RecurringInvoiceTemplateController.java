package com.invoiceme.recurring;

import com.invoiceme.domain.common.TemplateStatus;
import com.invoiceme.recurring.listtemplates.ListRecurringTemplatesHandler;
import com.invoiceme.recurring.listtemplates.ListRecurringTemplatesMapper;
import com.invoiceme.recurring.listtemplates.ListRecurringTemplatesQuery;
import com.invoiceme.recurring.listtemplates.PagedRecurringTemplateResponse;
import com.invoiceme.recurring.shared.RecurringTemplateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/recurring-invoices")
@RequiredArgsConstructor
public class RecurringInvoiceTemplateController {
    
    private final ListRecurringTemplatesHandler listHandler;
    private final ListRecurringTemplatesMapper listMapper;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT')")
    public ResponseEntity<PagedRecurringTemplateResponse> listRecurringTemplates(
        @RequestParam(required = false) UUID customerId,
        @RequestParam(required = false) TemplateStatus status,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "20") Integer size,
        @RequestParam(required = false) String sort
    ) {
        ListRecurringTemplatesQuery query = ListRecurringTemplatesQuery.builder()
            .customerId(customerId)
            .status(status)
            .page(page)
            .size(size)
            .sort(sort)
            .build();
        
        Page<com.invoiceme.domain.recurring.RecurringInvoiceTemplate> templatePage = listHandler.handle(query);
        
        List<RecurringTemplateDto> content = templatePage.getContent().stream()
            .map(listMapper::toDto)
            .collect(Collectors.toList());
        
        PagedRecurringTemplateResponse response = PagedRecurringTemplateResponse.builder()
            .content(content)
            .page(templatePage.getNumber())
            .size(templatePage.getSize())
            .totalElements(templatePage.getTotalElements())
            .totalPages(templatePage.getTotalPages())
            .first(templatePage.isFirst())
            .last(templatePage.isLast())
            .build();
        
        return ResponseEntity.ok(response);
    }
}


