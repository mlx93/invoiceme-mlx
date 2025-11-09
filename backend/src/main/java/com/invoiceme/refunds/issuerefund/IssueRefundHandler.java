package com.invoiceme.refunds.issuerefund;

import com.invoiceme.domain.common.DomainEventPublisher;
import com.invoiceme.domain.common.InvoiceStatus;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.events.RefundIssuedEvent;
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
public class IssueRefundHandler {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    @Transactional
    public Payment handle(IssueRefundCommand command) {
        // Load invoice
        Invoice invoice = invoiceRepository.findById(command.getInvoiceId())
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + command.getInvoiceId()));
        
        // Validate invoice is PAID
        if (invoice.getStatus() != InvoiceStatus.PAID) {
            throw new IllegalStateException("Can only issue refund for PAID invoices. Current status: " + invoice.getStatus());
        }
        
        // Validate refund amount doesn't exceed amount paid
        if (command.getAmount().isGreaterThan(invoice.getAmountPaid())) {
            throw new IllegalArgumentException(
                "Refund amount cannot exceed amount paid. Amount paid: " + invoice.getAmountPaid() + 
                ", Refund requested: " + command.getAmount()
            );
        }
        
        // Load customer
        Customer customer = customerRepository.findById(invoice.getCustomerId())
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + invoice.getCustomerId()));
        
        // Record refund on invoice (reduces amountPaid, updates balance)
        invoice.recordRefund(command.getAmount());
        
        // Create refund payment record using factory method
        Payment refund = Payment.createRefund(
            invoice,
            customer,
            command.getAmount(),
            command.getCreatedByUserId(),
            command.getReason()
        );
        
        // Save refund payment
        Payment savedRefund = paymentRepository.save(refund);
        
        // Save invoice (balance updated by recordRefund)
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        // Apply as credit if requested
        if (Boolean.TRUE.equals(command.getApplyAsCredit())) {
            customer.applyCredit(command.getAmount());
            customerRepository.save(customer);
        }
        
        // Publish RefundIssuedEvent
        savedRefund.addDomainEvent(new RefundIssuedEvent(
            savedRefund.getId(),
            invoice.getId(),
            invoice.getInvoiceNumber().toString(),
            customer.getId(),
            customer.getCompanyName(),
            customer.getEmail() != null ? customer.getEmail().getValue() : null,
            command.getAmount(),
            command.getReason(),
            command.getApplyAsCredit(),
            savedInvoice.getBalanceDue(),
            savedInvoice.getStatus().name()
        ));
        
        // Publish domain events
        eventPublisher.publishEvents(savedRefund);
        eventPublisher.publishEvents(savedInvoice);
        eventPublisher.publishEvents(customer);
        
        return savedRefund;
    }
}

