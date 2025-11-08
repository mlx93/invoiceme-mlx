package com.invoiceme.dashboard.getagingreport;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAgingReportHandler {
    
    private final InvoiceRepository invoiceRepository;
    
    public AgingReportResponse handle(GetAgingReportQuery query) {
        LocalDate today = LocalDate.now();
        
        // Get all outstanding invoices (SENT or OVERDUE)
        var outstandingInvoices = invoiceRepository.findByStatus(
            InvoiceStatus.SENT,
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        ).getContent();
        outstandingInvoices.addAll(
            invoiceRepository.findByStatus(
                InvoiceStatus.OVERDUE,
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
            ).getContent()
        );
        
        List<AgingReportResponse.AgingBucket> buckets = new ArrayList<>();
        
        // 0-30 days
        var bucket0_30 = outstandingInvoices.stream()
            .filter(invoice -> {
                long days = ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                return days >= 0 && days <= 30;
            })
            .toList();
        buckets.add(createBucket("0-30", bucket0_30));
        
        // 31-60 days
        var bucket31_60 = outstandingInvoices.stream()
            .filter(invoice -> {
                long days = ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                return days >= 31 && days <= 60;
            })
            .toList();
        buckets.add(createBucket("31-60", bucket31_60));
        
        // 61-90 days
        var bucket61_90 = outstandingInvoices.stream()
            .filter(invoice -> {
                long days = ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                return days >= 61 && days <= 90;
            })
            .toList();
        buckets.add(createBucket("61-90", bucket61_90));
        
        // 90+ days
        var bucket90Plus = outstandingInvoices.stream()
            .filter(invoice -> {
                long days = ChronoUnit.DAYS.between(invoice.getDueDate(), today);
                return days > 90;
            })
            .toList();
        buckets.add(createBucket("90+", bucket90Plus));
        
        return AgingReportResponse.builder()
            .buckets(buckets)
            .build();
    }
    
    private AgingReportResponse.AgingBucket createBucket(String range, List<Invoice> invoices) {
        Money totalAmount = invoices.stream()
            .map(Invoice::getBalanceDue)
            .reduce(Money.zero(), Money::add);
        
        return AgingReportResponse.AgingBucket.builder()
            .range(range)
            .invoiceCount(invoices.size())
            .totalAmount(totalAmount)
            .build();
    }
}

