package com.invoiceme.integration;

import com.invoiceme.domain.common.CustomerStatus;
import com.invoiceme.domain.common.CustomerType;
import com.invoiceme.domain.common.Money;
import com.invoiceme.domain.common.PaymentMethod;
import com.invoiceme.domain.common.PaymentTerms;
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

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomerPaymentFlowTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private InvoiceRepository invoiceRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        // Create test customer
        customer = Customer.create(
            "Test Company",
            com.invoiceme.domain.common.Email.of("test@example.com"),
            CustomerType.COMMERCIAL
        );
        customer = customerRepository.save(customer);
    }
    
    @Test
    void testCustomerToInvoiceToPaymentE2EFlow() {
        // Step 1: Create Invoice
        Invoice invoice = Invoice.create(
            customer.getId(),
            com.invoiceme.domain.common.InvoiceNumber.generate(1),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            PaymentTerms.NET_30
        );
        
        // Add line items
        invoice.addLineItem(com.invoiceme.domain.invoice.LineItem.create(
            "Test Item",
            2,
            Money.of(100.00),
            com.invoiceme.domain.common.DiscountType.NONE,
            Money.zero(),
            java.math.BigDecimal.valueOf(10), // 10% tax (passed as whole number, domain divides by 100)
            0
        ));
        
        invoice = invoiceRepository.save(invoice);
        
        // Verify invoice totals
        assertThat(invoice.getSubtotal().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(200.00));
        assertThat(invoice.getTaxAmount().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(20.00)); // 200 * (10/100) = 20
        assertThat(invoice.getTotalAmount().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(220.00)); // 200 + 20
        assertThat(invoice.getBalanceDue().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(220.00));
        
        // Step 2: Mark Invoice as Sent
        invoice.markAsSent();
        invoice = invoiceRepository.save(invoice);
        
        assertThat(invoice.getStatus()).isEqualTo(com.invoiceme.domain.common.InvoiceStatus.SENT);
        assertThat(invoice.getSentDate()).isNotNull();
        
        // Step 3: Record Payment
        Money paymentAmount = Money.of(220.00); // Full payment amount
        Payment payment = Payment.record(
            invoice,
            customer,
            paymentAmount,
            PaymentMethod.CREDIT_CARD,
            LocalDate.now(),
            UUID.randomUUID() // createdByUserId
        );
        
        payment = paymentRepository.save(payment);
        invoice = invoiceRepository.save(invoice);
        
        // Verify payment recorded
        assertThat(payment.getAmount()).isEqualTo(paymentAmount);
        assertThat(payment.getStatus()).isEqualTo(com.invoiceme.domain.common.PaymentStatus.COMPLETED);
        
        // Verify invoice updated
        assertThat(invoice.getAmountPaid().getAmount()).isEqualByComparingTo(java.math.BigDecimal.valueOf(220.00));
        assertThat(invoice.getBalanceDue().getAmount()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        assertThat(invoice.getStatus()).isEqualTo(com.invoiceme.domain.common.InvoiceStatus.PAID);
        assertThat(invoice.getPaidDate()).isNotNull();
    }
}

