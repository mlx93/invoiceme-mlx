package com.invoiceme.integration;

import com.invoiceme.domain.common.*;
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.domain.invoice.Invoice;
import com.invoiceme.domain.payment.Payment;
import com.invoiceme.infrastructure.persistence.CustomerRepository;
import com.invoiceme.infrastructure.persistence.InvoiceRepository;
import com.invoiceme.infrastructure.persistence.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InvoiceStateTransitionTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        customer = Customer.create(
            "State Test Company",
            Email.of("state@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testValidTransition_DraftToSent() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        
        // Transition to SENT
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getSentDate()).isNotNull();
    }
    
    @Test
    void testValidTransition_SentToPaid() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        
        // Record payment and transition to PAID
        Payment payment = Payment.record(
            invoice,
            customer,
            Money.of(100.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        paymentRepository.save(payment);
        
        invoice.recordPayment(Money.of(100.00));
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidDate()).isNotNull();
        assertThat(invoice.getBalanceDue().isZero()).isTrue();
    }
    
    // Note: Refunds are handled at the application layer via IssueRefundHandler,
    // not at the domain layer. Refunds create customer credits but don't change invoice status.
    // This test is removed as refunds are not a domain operation.
    
    @Test
    void testValidTransition_DraftToCancelled() {
        // Create draft invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        
        // Cancel invoice
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }
    
    @Test
    void testValidTransition_SentToCancelled() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        
        // Cancel invoice
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
    }
    
    @Test
    void testInvalidTransition_CannotSendAlreadySentInvoice() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to send again
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.markAsSent()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("already been sent");
    }
    
    @Test
    void testInvalidTransition_CannotCancelPaidInvoice() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(100.00));
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        
        // Attempt to cancel paid invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.cancel()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot cancel");
    }
    
    @Test
    void testInvalidTransition_CannotCancelCancelledInvoice() {
        // Create and cancel invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        
        // Attempt to cancel again
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.cancel()
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot cancel");
    }
    
    @Test
    void testInvalidTransition_CannotModifyCancelledInvoice() {
        // Create and cancel invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(100.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.cancel();
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to add line item to cancelled invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
                "New Item",
                1,
                Money.of(50.00),
                DiscountType.NONE,
                Money.zero(),
                java.math.BigDecimal.ZERO,
                0
            ))
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot add line items");
    }
    
    @Test
    void testPartialPaymentDoesNotChangeToPaid() {
        // Create and send invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Record partial payment
        Payment payment = Payment.record(
            invoice,
            customer,
            Money.of(400.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        paymentRepository.save(payment);
        
        invoice.recordPayment(Money.of(400.00));
        invoice = invoiceRepository.save(invoice);
        
        // Verify invoice stays SENT (not enough payment)
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(600.00));
        assertThat(invoice.getPaidDate()).isNull();
    }
}

