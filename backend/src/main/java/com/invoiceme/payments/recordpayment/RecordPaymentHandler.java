package com.invoiceme.payments.recordpayment;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecordPaymentHandler {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Payment handle(RecordPaymentCommand command) {
        // Load invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.getInvoiceId()));
        
        // Load customer
        Customer customer = customerRepository.findById(invoice.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + invoice.getCustomerId()));
        
        // Calculate overpayment before recording payment
        Money currentBalance = invoice.getBalanceDue();
        Money overpayment = command.getAmount().isGreaterThan(currentBalance) 
            ? command.getAmount().subtract(currentBalance)
            : com.invoiceme.domain.common.Money.zero();
        
        // Record payment using static factory method
        Payment payment = Payment.record(
            invoice,
            customer,
            command.getAmount(),
            command.getPaymentMethod(),
            command.getPaymentDate(),
            command.getCreatedByUserId()
        );
        
        // Set optional fields
        if (command.getPaymentReference() != null) {
            payment.updateReference(command.getPaymentReference());
        }
        if (command.getNotes() != null) {
            payment.updateNotes(command.getNotes());
        }
        
        // Save payment and invoice (invoice was modified by Payment.record())
        Payment savedPayment = paymentRepository.save(payment);
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Handle overpayment (excess goes to customer credit)
        if (overpayment.isPositive()) {
            customer.applyCredit(overpayment);
            customerRepository.save(customer);
        }
        
        // Publish domain events after transaction commit
        eventPublisher.publishEvents(savedPayment);
        eventPublisher.publishEvents(invoice);
        eventPublisher.publishEvents(customer);
        
        return savedPayment;
    }
}

