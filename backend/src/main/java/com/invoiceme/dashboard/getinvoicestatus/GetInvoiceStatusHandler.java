package com.invoiceme.dashboard.getinvoicestatus;

import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetInvoiceStatusHandler {
    
    private final InvoiceRepository invoiceRepository;
    
    public InvoiceStatusResponse handle(GetInvoiceStatusQuery query) {
        List<InvoiceStatusResponse.StatusBreakdown> breakdown = Arrays.stream(InvoiceStatus.values())
            .map(status -> {
                long count = invoiceRepository.countByStatus(status);
                var totalAmount = invoiceRepository.sumTotalAmountByStatus(status)
                    .orElse(java.math.BigDecimal.ZERO);
                
                return InvoiceStatusResponse.StatusBreakdown.builder()
                    .status(status.name())
                    .count((int) count)
                    .totalAmount(Money.of(totalAmount))
                    .build();
            })
            .collect(Collectors.toList());
        
        return InvoiceStatusResponse.builder()
            .breakdown(breakdown)
            .build();
    }
}

