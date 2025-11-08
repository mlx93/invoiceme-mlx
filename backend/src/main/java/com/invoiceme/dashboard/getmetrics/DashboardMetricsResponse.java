package com.invoiceme.dashboard.getmetrics;

import com.invoiceme.domain.common.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    private Money revenueMTD; // Revenue month-to-date
    private Integer outstandingInvoicesCount;
    private Money outstandingInvoicesAmount;
    private Integer overdueInvoicesCount;
    private Money overdueInvoicesAmount;
    private Integer activeCustomersCount;
    private LocalDate asOfDate; // Date metrics were calculated
}

