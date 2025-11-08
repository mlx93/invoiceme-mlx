package com.invoiceme.dashboard;

import com.invoiceme.dashboard.getagingreport.AgingReportResponse;
import com.invoiceme.dashboard.getagingreport.GetAgingReportQuery;
import com.invoiceme.dashboard.getagingreport.GetAgingReportHandler;
import com.invoiceme.dashboard.getinvoicestatus.GetInvoiceStatusQuery;
import com.invoiceme.dashboard.getinvoicestatus.GetInvoiceStatusHandler;
import com.invoiceme.dashboard.getinvoicestatus.InvoiceStatusResponse;
import com.invoiceme.dashboard.getmetrics.DashboardMetricsResponse;
import com.invoiceme.dashboard.getmetrics.GetMetricsQuery;
import com.invoiceme.dashboard.getmetrics.GetMetricsHandler;
import com.invoiceme.dashboard.getrevenuetrend.GetRevenueTrendQuery;
import com.invoiceme.dashboard.getrevenuetrend.GetRevenueTrendHandler;
import com.invoiceme.dashboard.getrevenuetrend.RevenueTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final GetMetricsHandler getMetricsHandler;
    private final GetRevenueTrendHandler getRevenueTrendHandler;
    private final GetInvoiceStatusHandler getInvoiceStatusHandler;
    private final GetAgingReportHandler getAgingReportHandler;
    
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {
        GetMetricsQuery query = new GetMetricsQuery();
        DashboardMetricsResponse response = getMetricsHandler.handle(query);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<RevenueTrendResponse> getRevenueTrend(
            @RequestParam(required = false) java.time.LocalDate startDate,
            @RequestParam(required = false) java.time.LocalDate endDate,
            @RequestParam(required = false) String period) {
        GetRevenueTrendQuery query = GetRevenueTrendQuery.builder()
            .startDate(startDate)
            .endDate(endDate)
            .period(period)
            .build();
        RevenueTrendResponse response = getRevenueTrendHandler.handle(query);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/invoice-status")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<InvoiceStatusResponse> getInvoiceStatus() {
        GetInvoiceStatusQuery query = new GetInvoiceStatusQuery();
        InvoiceStatusResponse response = getInvoiceStatusHandler.handle(query);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/aging-report")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'ACCOUNTANT', 'SALES')")
    public ResponseEntity<AgingReportResponse> getAgingReport() {
        GetAgingReportQuery query = new GetAgingReportQuery();
        AgingReportResponse response = getAgingReportHandler.handle(query);
        return ResponseEntity.ok(response);
    }
}

