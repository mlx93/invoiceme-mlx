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
    private Money totalRevenueMTD; // Revenue month-to-date (renamed to match frontend)
    private Integer outstandingInvoicesCount;
    private Money outstandingInvoicesAmount;
    private Integer overdueInvoicesCount;
    private Money overdueInvoicesAmount;
    private Integer activeCustomers; // Renamed from activeCustomersCount to match frontend
    private LocalDate asOfDate; // Date metrics were calculated
}

