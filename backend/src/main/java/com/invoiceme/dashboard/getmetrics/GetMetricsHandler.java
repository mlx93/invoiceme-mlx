package com.invoiceme.dashboard.getmetrics;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMetricsHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    
    @Cacheable(value = "dashboardMetrics", key = "'metrics'")
    public DashboardMetricsResponse handle(GetMetricsQuery query) {
        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        
        // Revenue MTD (from PAID invoices issued this month)
        var revenueMTD = invoiceRepository.sumTotalAmountByStatusAndDateRange(
            InvoiceStatus.PAID,
            firstOfMonth,
            today
        ).orElse(java.math.BigDecimal.ZERO);
        Money revenueMTDMoney = Money.of(revenueMTD);
        
        // Outstanding invoices (SENT or OVERDUE status)
        List<Invoice> outstandingInvoices = new ArrayList<>(
            invoiceRepository.findByStatus(
                InvoiceStatus.SENT,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
            ).getContent()
        );
        outstandingInvoices.addAll(
            invoiceRepository.findByStatus(
                InvoiceStatus.OVERDUE,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
            ).getContent()
        );
        
        int outstandingCount = outstandingInvoices.size();
        Money outstandingAmount = outstandingInvoices.stream()
            .map(invoice -> invoice.getBalanceDue())
            .reduce(Money.zero(), Money::add);
        
        // Overdue invoices
        var overdueInvoices = invoiceRepository.findOverdueInvoices(today);
        int overdueCount = overdueInvoices.size();
        Money overdueAmount = overdueInvoices.stream()
            .map(invoice -> invoice.getBalanceDue())
            .reduce(Money.zero(), Money::add);
        
        // Active customers
        long activeCustomersCount = customerRepository.countByStatus(
            com.invoiceme.domain.common.CustomerStatus.ACTIVE
        );
        
        return DashboardMetricsResponse.builder()
            .totalRevenueMTD(revenueMTDMoney)
            .outstandingInvoicesCount(outstandingCount)
            .outstandingInvoicesAmount(outstandingAmount)
            .overdueInvoicesCount(overdueCount)
            .overdueInvoicesAmount(overdueAmount)
            .activeCustomers((int) activeCustomersCount)
            .asOfDate(today)
            .build();
    }
}

