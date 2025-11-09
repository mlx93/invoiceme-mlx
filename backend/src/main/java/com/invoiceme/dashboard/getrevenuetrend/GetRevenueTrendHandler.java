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
        
        List<RevenueTrendResponse.RevenueTrendData> data = new ArrayList<>();
        
        if ("MONTHLY".equals(period)) {
            // Group by month using paidDate (when revenue was actually received)
            Map<String, List<Invoice>> byMonth = invoices.stream()
                .filter(invoice -> invoice.getPaidDate() != null) // Only include invoices with paid date
                .collect(Collectors.groupingBy(
                    invoice -> {
                        // Convert Instant to LocalDate and format as YYYY-MM
                        LocalDate paidDate = invoice.getPaidDate()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        return paidDate.withDayOfMonth(1).toString().substring(0, 7); // "YYYY-MM"
                    }
                ));
            
            byMonth.forEach((monthStr, monthInvoices) -> {
                Money revenue = monthInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(Money.zero(), Money::add);
                data.add(RevenueTrendResponse.RevenueTrendData.builder()
                    .month(monthStr)
                    .revenue(revenue)
                    .build());
            });
        } else if ("WEEKLY".equals(period)) {
            // Group by week using paidDate
            Map<String, List<Invoice>> byWeek = invoices.stream()
                .filter(invoice -> invoice.getPaidDate() != null) // Only include invoices with paid date
                .collect(Collectors.groupingBy(
                    invoice -> {
                        // Convert Instant to LocalDate and format as YYYY-MM for weekly grouping
                        LocalDate paidDate = invoice.getPaidDate()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        return paidDate.toString().substring(0, 7); // "YYYY-MM" (simplified weekly grouping)
                    }
                ));
            
            byWeek.forEach((weekStr, weekInvoices) -> {
                Money revenue = weekInvoices.stream()
                    .map(Invoice::getTotalAmount)
                    .reduce(Money.zero(), Money::add);
                data.add(RevenueTrendResponse.RevenueTrendData.builder()
                    .month(weekStr)
                    .revenue(revenue)
                    .build());
            });
        }
        
        return RevenueTrendResponse.builder()
            .data(data)
            .build();
    }
}

