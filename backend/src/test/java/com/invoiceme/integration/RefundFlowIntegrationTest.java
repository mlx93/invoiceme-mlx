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
public class RefundFlowIntegrationTest {
    
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
            "Refund Test Company",
            Email.of("refund@test.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testFullRefund() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(1000.00));
        invoice = invoiceRepository.save(invoice);
        
        // Record payment
        Payment payment = Payment.record(
            invoice,
            customer,
            Money.of(1000.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        paymentRepository.save(payment);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getBalanceDue().isZero()).isTrue();
        
        // Issue full refund
        invoice.issueRefund(
            Money.of(1000.00),
            "Customer requested full refund"
        );
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT); // Back to SENT
        assertThat(invoice.getAmountPaid().isZero()).isTrue(); // Payment reversed
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00)); // Full balance due
        assertThat(invoice.getPaidDate()).isNull(); // No longer paid
    }
    
    @Test
    void testPartialRefund() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(1000.00));
        invoice = invoiceRepository.save(invoice);
        
        Payment payment = Payment.record(
            invoice,
            customer,
            Money.of(1000.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        paymentRepository.save(payment);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        
        // Issue partial refund of $300
        invoice.issueRefund(
            Money.of(300.00),
            "Partial refund - service adjustment"
        );
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT); // Back to SENT
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(700.00)); // 1000 - 300
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00)); // Remaining balance
        assertThat(invoice.getPaidDate()).isNull(); // No longer fully paid
    }
    
    @Test
    void testMultiplePartialRefunds() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(1000.00));
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        
        // First partial refund
        invoice.issueRefund(Money.of(200.00), "First refund");
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(800.00));
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(200.00));
        
        // Second partial refund
        invoice.issueRefund(Money.of(150.00), "Second refund");
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(650.00));
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(350.00));
        
        // Third partial refund
        invoice.issueRefund(Money.of(100.00), "Third refund");
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(550.00));
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(450.00));
    }
    
    @Test
    void testCannotRefundMoreThanPaid() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(500.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(500.00));
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to refund more than paid
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.issueRefund(
                Money.of(600.00),
                "Excessive refund"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Refund amount exceeds amount paid");
    }
    
    @Test
    void testCannotRefundUnpaidInvoice() {
        // Create sent but unpaid invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(500.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to refund unpaid invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.issueRefund(
                Money.of(100.00),
                "Cannot refund unpaid"
            )
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Refund amount exceeds amount paid");
    }
    
    @Test
    void testRefundOnPartiallyPaidInvoice() {
        // Create invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        
        // Partial payment of $600
        invoice.recordPayment(Money.of(600.00));
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(600.00));
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(400.00));
        
        // Refund $200 of the $600 paid
        invoice.issueRefund(Money.of(200.00), "Partial refund");
        invoice = invoiceRepository.save(invoice);
        
        // Verify
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(400.00)); // 600 - 200
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(600.00)); // 1000 - 400
    }
    
    @Test
    void testRefundThenAdditionalPayment() {
        // Create, send, and pay invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(1000.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.markAsSent();
        invoice.recordPayment(Money.of(1000.00));
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        
        // Issue partial refund
        invoice.issueRefund(Money.of(300.00), "Partial refund");
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        assertThat(invoice.getBalanceDue().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(300.00));
        
        // Make additional payment to cover new balance
        invoice.recordPayment(Money.of(300.00));
        invoice = invoiceRepository.save(invoice);
        
        Payment additionalPayment = Payment.record(
            invoice,
            customer,
            Money.of(300.00),
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID()
        );
        paymentRepository.save(additionalPayment);
        
        // Verify back to PAID status
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getBalanceDue().isZero()).isTrue();
        assertThat(invoice.getAmountPaid().getAmount())
            .isEqualByComparingTo(java.math.BigDecimal.valueOf(1000.00));
    }
    
    @Test
    void testCannotRefundCancelledInvoice() {
        // Create and cancel invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Service",
            1,
            Money.of(500.00),
            DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.ZERO,
            0
        ));
        
        invoice.cancel("Cancelled order");
        invoice = invoiceRepository.save(invoice);
        
        // Attempt to refund cancelled invoice
        Invoice finalInvoice = invoice;
        assertThatThrownBy(() -> 
            finalInvoice.issueRefund(
                Money.of(100.00),
                "Cannot refund cancelled"
            )
        ).isInstanceOf(IllegalStateException.class)
         .hasMessageContaining("Cannot issue refund");
    }
}

