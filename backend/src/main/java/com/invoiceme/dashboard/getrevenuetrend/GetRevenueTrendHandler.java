package com.invoiceme.dashboard.getrevenuetrend;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetRevenueTrendHandler {
    
    private final InvoiceRepository invoiceRepository;
    
    public RevenueTrendResponse handle(GetRevenueTrendQuery query) {
        LocalDate startDate = query.getStartDate() != null 
            ? query.getStartDate() 
            : LocalDate.now().minusMonths(12);
        LocalDate endDate = query.getEndDate() != null 
            ? query.getEndDate() 
            : LocalDate.now();
        String period = query.getPeriod() != null ? query.getPeriod() : "MONTHLY";
        
        // Get all PAID invoices in date range
        var invoices = invoiceRepository.findByFilters(
            List.of(InvoiceStatus.PAID),
            null,
            startDate,
            endDate,
            null,
            null,
            null,
            null,
            null,
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();
        
        List<RevenueTrendResponse.RevenueDataPoint> dataPoints = new ArrayList<>();
        
        if ("MONTHLY".equals(period)) {
            // Group by month
            Map<String, List<Invoice>> byMonth = invoices.stream()
                .collect(Collectors.groupingBy(
                    invoice -> invoice.getIssueDate().withDayOfMonth(1).toString()
                ));
            
            byMonth.forEach((monthStr, monthInvoices) -> {
                LocalDate month = LocalDate.parse(monthStr + "-01");
                Money revenue = monthInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(Money.zero(), Money::add);
                dataPoints.add(RevenueTrendResponse.RevenueDataPoint.builder()
                    .period(month)
                    .revenue(revenue)
                    .invoiceCount(monthInvoices.size())
                    .build());
            });
        } else if ("WEEKLY".equals(period)) {
            // Group by week
            Map<String, List<Invoice>> byWeek = invoices.stream()
                .collect(Collectors.groupingBy(
                    invoice -> invoice.getIssueDate().toString().substring(0, 10) // Simplified
                ));
            
            byWeek.forEach((weekStr, weekInvoices) -> {
                LocalDate week = LocalDate.parse(weekStr);
                Money revenue = weekInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(Money.zero(), Money::add);
                dataPoints.add(RevenueTrendResponse.RevenueDataPoint.builder()
                    .period(week)
                    .revenue(revenue)
                    .invoiceCount(weekInvoices.size())
                    .build());
            });
        }
        
        return RevenueTrendResponse.builder()
            .dataPoints(dataPoints)
            .build();
    }
}

