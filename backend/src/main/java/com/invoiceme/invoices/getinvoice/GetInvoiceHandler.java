package com.invoiceme.invoices.getinvoice;

import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetInvoiceHandler {
    
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    
    public InvoiceDetailResult handle(GetInvoiceQuery query) {
        Invoice invoice = invoiceRepository.findById(query.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + query.getInvoiceId()));
        
        // Load line items (they're lazy-loaded)
        invoice.getLineItems().size(); // Force load
        
        // Get payments for this invoice
        var payments = paymentRepository.findByInvoiceId(
            invoice.getId(),
            org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)
        );
        
        return InvoiceDetailResult.builder()
            .invoice(invoice)
            .payments(payments.getContent())
            .build();
    }
}

