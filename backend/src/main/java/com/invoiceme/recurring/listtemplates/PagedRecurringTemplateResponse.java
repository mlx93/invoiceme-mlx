package com.invoiceme.recurring.listtemplates;

import com.invoiceme.recurring.shared.RecurringTemplateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedRecurringTemplateResponse {
    private List<RecurringTemplateDto> content;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;
}


